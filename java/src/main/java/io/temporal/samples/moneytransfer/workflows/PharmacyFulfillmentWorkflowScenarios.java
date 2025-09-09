package io.temporal.samples.moneytransfer.workflows;

import io.temporal.common.SearchAttributeKey;
import io.temporal.common.converter.EncodedValues;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.activities.PharmacyFulfillmentActivities;
import io.temporal.samples.moneytransfer.model.AdjudicationResponse;
import io.temporal.samples.moneytransfer.model.PrescriptionInput;
import io.temporal.samples.moneytransfer.model.PrescriptionOutput;
import io.temporal.samples.moneytransfer.model.PrescriptionStatus;
import io.temporal.workflow.DynamicWorkflow;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

public class PharmacyFulfillmentWorkflowScenarios implements DynamicWorkflow {

    private static final String BUG = "PharmacyFulfillmentWorkflowRecoverableFailure";
    private static final String NEEDS_APPROVAL = "PharmacyFulfillmentWorkflowHumanInLoop";
    private static final String ADVANCED_VISIBILITY = "PharmacyFulfillmentWorkflowAdvancedVisibility";

    private static final Logger log = Workflow.getLogger(PharmacyFulfillmentWorkflowScenarios.class);

    private final SearchAttributeKey<String> WORKFLOW_STEP = SearchAttributeKey.forKeyword("Step");

    private final PharmacyFulfillmentActivities activities = Workflow.newActivityStub(
        PharmacyFulfillmentActivities.class,
        PharmacyFulfillmentActivities.activityOptions
    );

    private int progress = 0;
    private String prescriptionState = "starting";
    private AdjudicationResponse adjudicationResponse = new AdjudicationResponse("", "", 0.0, "");

    private int approvalTime = 30;
    private boolean approved = false;

    @Override
    public Object execute(EncodedValues args) {
        Workflow.registerListener(new PharmacyFulfillmentDynamicListenerImpl());
        PrescriptionInput input = args.get(0, PrescriptionInput.class);
        String type = Workflow.getInfo().getWorkflowType();
        log.info("Dynamic Pharmacy Fulfillment workflow started, type = {}", type);
        String idempotencyKey = Workflow.randomUUID().toString();

        // Verify Order
        upsertStep("VerifyOrder");
        activities.verifyOrder(input);
        updateProgress(25, 1);

        if (NEEDS_APPROVAL.equals(type)) {
            log.info(
                "Waiting on 'approvePrescription' Signal or Update for workflow ID: {}",
                Workflow.getInfo().getWorkflowId()
            );
            updateProgress(30, 0, "waiting");

            // Wait for the approval for up to approvalTime
            boolean receivedApproval = Workflow.await(Duration.ofSeconds(approvalTime), () -> approved);

            // If the approval was not received within the timeout, fail the workflow
            if (!receivedApproval) {
                log.error(
                    "Approval not received within the {}-second time window: Failing the workflow.",
                    approvalTime
                );
                throw ApplicationFailure.newFailure(
                    "Approval not received within " + approvalTime + " seconds",
                    "ApprovalTimeout"
                );
            }
        }

        // Load Prescription to Pharmacy System
        upsertStep("LoadPrescriptionToPharmacySystem");
        activities.loadPrescriptionToPharmacySystem(idempotencyKey, input, type);
        updateProgress(50, 2);

        if (BUG.equals(type)) {
            // Simulate bug
            throw new RuntimeException("Simulated bug - fix me!");
        }

        // Adjudication
        upsertStep("Adjudication");
        try {
            adjudicationResponse = activities.adjudication(idempotencyKey, input, type);
            updateProgress(75, 2);
        } catch (ActivityFailure e) {
            // if adjudication fails in an unrecoverable way, rollback the pharmacy system load and fail the workflow
            log.info("Adjudication failed unrecoverable error, reverting pharmacy system load");

            // Reverse Pharmacy System Load (rollback)
            activities.reversePharmacySystemLoad(input);

            // return failure message
            String message = ((ApplicationFailure) e.getCause()).getOriginalMessage();
            throw ApplicationFailure.newNonRetryableFailure(message, "AdjudicationFailed");
        }

        // Notify Customer
        upsertStep("NotifyCustomer");
        activities.notifyCustomer(input);
        updateProgress(100, 1, "fulfilled");

        return new PrescriptionOutput(adjudicationResponse, "READY_FOR_PICKUP", input.getPharmacyId());
    }

    private void upsertStep(String step) {
        if (ADVANCED_VISIBILITY.equals(Workflow.getInfo().getWorkflowType())) {
            log.info("Advanced visibility .. {}", step);
            Workflow.upsertTypedSearchAttributes(WORKFLOW_STEP.valueSet(step));
        }
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

    class PharmacyFulfillmentDynamicListenerImpl implements PharmacyFulfillmentMessages {

        @Override
        public PrescriptionStatus queryPrescriptionStatus() {
            return new PrescriptionStatus(progress, prescriptionState, "", adjudicationResponse, approvalTime);
        }

        @Override
        public void approvePrescriptionSignal() {
            log.info("Approve Signal Received");

            if (prescriptionState.equals("waiting")) {
                approved = true;
            } else {
                log.info("Signal not applied: Prescription is not waiting for approval.");
            }
        }

        @Override
        public String approvePrescriptionUpdate() {
            log.info("Approve Update Validated: Approving Prescription");
            approved = true;
            return "successfully approved prescription";
        }

        @Override
        public void approvePrescriptionUpdateValidator() {
            log.info("Approve Update Received: Validating");
            if (approved) {
                throw new IllegalStateException("Validation Failed: Prescription already approved");
            }
            if (!prescriptionState.equals("waiting")) {
                throw new IllegalStateException("Validation Failed: Prescription doesn't require approval");
            }
        }
    }
}
