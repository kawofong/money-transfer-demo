package io.temporal.samples.moneytransfer.activities;

import io.temporal.activity.Activity;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.model.AdjudicationResponse;
import io.temporal.samples.moneytransfer.model.PrescriptionInput;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class PharmacyFulfillmentActivitiesImpl implements PharmacyFulfillmentActivities {

    private static final String PHARMACY_SYSTEM_DOWNTIME = "PharmacyFulfillmentWorkflowAPIDowntime";
    private static final String INVALID_PRESCRIPTION = "PharmacyFulfillmentWorkflowInvalidPrescription";

    private static void simulateExternalOperation(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String simulateExternalOperation(long ms, String type, int attempt) {
        simulateExternalOperation(ms / attempt);
        return (attempt < 5) ? type : "NoError";
    }

    @Override
    public String verifyOrder(PrescriptionInput input) {
        log.info("Verify order activity started, input = {}", input);

        // simulate external API call to verify prescription validity
        simulateExternalOperation(1500);
        
        log.info("Order verification complete for prescription ID: {}", input.getPrescriptionId());
        return "SUCCESS";
    }

    @Override
    public String loadPrescriptionToPharmacySystem(String idempotencyKey, PrescriptionInput input, String type) {
        log.info("Load prescription to pharmacy system started, prescription ID = {}", input.getPrescriptionId());
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();

        // simulate external API call to pharmacy management system
        String error = simulateExternalOperation(2000, type, attempt);
        log.info("Pharmacy system load complete, type = {}, error = {}", type, error);

        if (PHARMACY_SYSTEM_DOWNTIME.equals(error)) {
            // a transient error, which can be retried
            log.info("Pharmacy system unavailable, attempt = {}", attempt);
            throw new RuntimeException("Load prescription activity failed, pharmacy system unavailable");
        }

        return "SUCCESS";
    }

    @Override
    public AdjudicationResponse adjudication(String idempotencyKey, PrescriptionInput input, String type) {
        log.info("Adjudication activity started, prescription ID = {}", input.getPrescriptionId());
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();

        // simulate external API call to insurance adjudication system
        String error = simulateExternalOperation(3000, type, attempt);
        log.info("Adjudication call complete, type = {}, error = {}", type, error);

        if (INVALID_PRESCRIPTION.equals(error)) {
            // a business error, which cannot be retried
            throw ApplicationFailure.newNonRetryableFailure(
                    "Adjudication activity failed, prescription is invalid or not covered",
                    "InvalidPrescription"
            );
        }

        return new AdjudicationResponse(
            "adj-" + input.getPrescriptionId() + "-" + System.currentTimeMillis(),
            "80% covered",
            15.99,
            "APPROVED"
        );
    }

    @Override
    public String notifyCustomer(PrescriptionInput input) {
        log.info("Notify customer activity started, patient ID = {}", input.getPatientId());

        // simulate external API call to notification service
        simulateExternalOperation(1000);
        
        log.info("Customer notification sent for prescription ID: {}", input.getPrescriptionId());
        return "SUCCESS";
    }

    @Override
    public boolean reversePharmacySystemLoad(PrescriptionInput input) {
        log.info("Reverse pharmacy system load started, prescription ID = {}", input.getPrescriptionId());

        // simulate external API call to remove prescription from pharmacy system
        simulateExternalOperation(1500);
        
        log.info("Pharmacy system load reversed for prescription ID: {}", input.getPrescriptionId());
        return true;
    }
}
