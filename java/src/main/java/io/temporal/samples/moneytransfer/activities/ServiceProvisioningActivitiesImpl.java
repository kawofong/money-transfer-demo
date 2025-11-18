package io.temporal.samples.moneytransfer.activities;

import io.temporal.activity.Activity;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningInput;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ServiceProvisioningActivitiesImpl implements ServiceProvisioningActivities {

    private static final String API_DOWNTIME = "ServiceProvisioningWorkflowAPIDowntime";
    private static final String INVALID_ACCOUNT = "ServiceProvisioningWorkflowInvalidAccount";

    private static void simulateExternalOperation(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation interrupted", e);
        }
    }

    private static String simulateExternalOperation(long ms, String type, int attempt) {
        simulateExternalOperation(ms / attempt);
        return (attempt < 5) ? type : "NoError";
    }

    @Override
    public String validateServiceRequest(ServiceProvisioningInput input) {
        log.info("Validating service request for customer: {}", input.getCustomerId());

        // Simulate external API call to customer management system
        simulateExternalOperation(1500);

        return "SERVICE_REQUEST_VALIDATED";
    }

    @Override
    public String reserveResources(String idempotencyKey, ServiceProvisioningInput input) {
        log.info("Reserving resources for {} {} service", input.getServiceType(), input.getServiceLevel());

        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        String workflowType = Activity.getExecutionContext().getInfo().getWorkflowType();

        // Simulate external API call to network management system
        String error = simulateExternalOperation(2000, workflowType, attempt);
        log.info("Resource reservation call complete, type = {}, error = {}", workflowType, error);

        if (API_DOWNTIME.equals(error)) {
            throw new RuntimeException("Resource reservation failed, network management API unavailable");
        }

        return "RESOURCES_RESERVED";
    }

    @Override
    public String configureService(String idempotencyKey, ServiceProvisioningInput input) {
        log.info("Configuring {} service for customer: {}", input.getServiceType(), input.getCustomerId());

        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        String workflowType = Activity.getExecutionContext().getInfo().getWorkflowType();

        // Simulate external API call to service configuration system
        String error = simulateExternalOperation(2500, workflowType, attempt);

        if (INVALID_ACCOUNT.equals(error)) {
            throw ApplicationFailure.newNonRetryableFailure(
                    "Service configuration failed, customer account is invalid",
                    "InvalidCustomer"
            );
        }

        return "SERVICE_CONFIGURED";
    }

    @Override
    public String activateService(ServiceProvisioningInput input) {
        log.info("Activating {} service for customer: {}", input.getServiceType(), input.getCustomerId());

        // Simulate external API call to service activation system
        simulateExternalOperation(1500);

        return "SERVICE_ACTIVATED";
    }

    @Override
    public boolean undoReservation(ServiceProvisioningInput input) {
        log.info("Releasing reserved resources for customer: {}", input.getCustomerId());

        // Simulate external API call to release resources
        simulateExternalOperation(1000);

        return true;
    }
}
