package io.temporal.samples.moneytransfer.activities;

import io.temporal.activity.Activity;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.model.DepositResponse;
import io.temporal.samples.moneytransfer.model.TransferInput;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class AccountTransferActivitiesImpl implements AccountTransferActivities {

    private static final String API_DOWNTIME = "AccountTransferWorkflowAPIDowntime";
    private static final String INVALID_ACCOUNT = "AccountTransferWorkflowInvalidAccount";
    private static final String NETWORK_CAPACITY_UNAVAILABLE = "NetworkCapacityUnavailable";
    private static final String INVALID_SERVICE_REQUEST = "InvalidServiceRequest";

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
    public String validateServiceRequest(TransferInput input) {
        log.info("Validate service request activity started, input = {}", input);
        log.info("Checking customer eligibility, credit score, and service availability for account: {}", input.getFromAccount());

        // simulate external API call to customer management system
        simulateExternalOperation(1000);

        // Simulate validation logic
        log.info("Customer eligibility verified");
        log.info("Credit check passed");
        log.info("Service availability confirmed in area");

        return "SERVICE_REQUEST_VALIDATED";
    }

    @Override
    public String reserveResources(String idempotencyKey, float amount, String type) {
        log.info("Reserve resources activity started, service level = {}", amount);
        log.info("Reserving network capacity, equipment, and installation slots with key: {}", idempotencyKey);
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();

        // simulate external API call to network management system
        String error = simulateExternalOperation(1000, type, attempt);
        log.info("Resource reservation call complete, type = {}, error = {}", type, error);

        if (API_DOWNTIME.equals(error)) {
            // a transient error, which can be retried
            log.info("Network management API unavailable, attempt = {}", attempt);
            throw new RuntimeException("Resource reservation failed, network management API unavailable");
        }

        if (NETWORK_CAPACITY_UNAVAILABLE.equals(error)) {
            // a transient error that might resolve with retry
            log.info("Network capacity temporarily unavailable, attempt = {}", attempt);
            throw new RuntimeException("Network capacity unavailable, retrying...");
        }

        log.info("Successfully reserved network bandwidth, equipment slot, and installation appointment");
        return "RESOURCES_RESERVED";
    }

    @Override
    public DepositResponse configureService(String idempotencyKey, float amount, String type) {
        log.info("Configure service activity started, service level = {}", amount);
        log.info("Configuring network elements, assigning phone numbers, setting up accounts with key: {}", idempotencyKey);
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();

        // simulate external API call to service configuration system
        String error = simulateExternalOperation(1000, type, attempt);
        log.info("Service configuration call complete, type = {}, error = {}", type, error);

        if (INVALID_ACCOUNT.equals(error)) {
            // a business error, which cannot be retried
            throw ApplicationFailure.newNonRetryableFailure(
                    "Service configuration failed, customer account is invalid",
                    "InvalidAccount"
            );
        }

        if (INVALID_SERVICE_REQUEST.equals(error)) {
            // a business error, which cannot be retried
            throw ApplicationFailure.newNonRetryableFailure(
                    "Service configuration failed, invalid service parameters",
                    "InvalidServiceRequest"
            );
        }

        log.info("Successfully configured network elements and assigned service identifiers");
        return new DepositResponse("example-transfer-id");
    }

    @Override
    public String activateService(TransferInput input) {
        log.info("Activate service activity started, input = {}", input);
        log.info("Enabling service for customer account: {} to account: {}", input.getFromAccount(), input.getToAccount());

        // simulate external API call to service activation system
        simulateExternalOperation(1000);

        log.info("Service activated successfully");
        log.info("Customer can now use their new service (modem plugged in, SIM card activated)");

        return "SERVICE_ACTIVATED";
    }

    @Override
    public boolean undoReservation(float amount) {
        log.info("Undo resource reservation activity started, service level = {}", amount);
        log.info("Releasing reserved network capacity, equipment, and installation slots");

        // simulate external API call to release resources
        simulateExternalOperation(1000);

        log.info("Successfully released all reserved resources");
        return true;
    }
}
