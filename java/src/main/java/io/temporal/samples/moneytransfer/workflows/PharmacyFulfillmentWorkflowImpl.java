package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.activities.PharmacyFulfillmentActivities;
import io.temporal.samples.moneytransfer.model.AdjudicationResponse;
import io.temporal.samples.moneytransfer.model.PrescriptionInput;
import io.temporal.samples.moneytransfer.model.PrescriptionOutput;
import io.temporal.samples.moneytransfer.model.PrescriptionStatus;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class PharmacyFulfillmentWorkflowImpl implements PharmacyFulfillmentWorkflow {

    private static final Logger log = Workflow.getLogger(PharmacyFulfillmentWorkflowImpl.class);

    private final PharmacyFulfillmentActivities activities = Workflow.newActivityStub(
            PharmacyFulfillmentActivities.class,
            PharmacyFulfillmentActivities.activityOptions
    );

    private int progress = 0;
    private String prescriptionState = "starting";
    private AdjudicationResponse adjudicationResponse = new AdjudicationResponse("", "", 0.0, "");

    @Override
    public PrescriptionOutput fulfillPrescription(PrescriptionInput input) {
        String type = Workflow.getInfo().getWorkflowType();
        log.info("Pharmacy Fulfillment workflow started, type = {}", type);
        String idempotencyKey = Workflow.randomUUID().toString();

        // Verify Order
        activities.verifyOrder(input);
        updateProgress(25, 1);

        // Load Prescription to Pharmacy System
        activities.loadPrescriptionToPharmacySystem(idempotencyKey, input, type);
        updateProgress(50, 2);

        // Adjudication
        adjudicationResponse = activities.adjudication(idempotencyKey, input, type);
        updateProgress(75, 2);

        // Notify Customer
        activities.notifyCustomer(input);
        updateProgress(100, 1, "fulfilled");

        return new PrescriptionOutput(adjudicationResponse, "READY_FOR_PICKUP", input.getPharmacyId());
    }

    @Override
    public PrescriptionStatus queryPrescriptionStatus() {
        return new PrescriptionStatus(progress, prescriptionState, "", adjudicationResponse, 0);
    }

    private void updateProgress(int progress, int sleep) {
        updateProgress(progress, sleep, "processing");
    }

    private void updateProgress(int progress, int sleep, String prescriptionState) {
        if (sleep > 0) {
            Workflow.sleep(Duration.ofSeconds(sleep));
        }
        this.prescriptionState = prescriptionState;
        this.progress = progress;
    }
}
