package io.temporal.samples.moneytransfer.workflows;

import io.temporal.common.SearchAttributeKey;
import io.temporal.common.converter.EncodedValues;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.samples.moneytransfer.activities.ServiceProvisioningActivities;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningInput;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningOutput;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningStatus;
import io.temporal.workflow.DynamicWorkflow;
import io.temporal.workflow.TimerOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

public class ServiceProvisioningWorkflowScenarios implements DynamicWorkflow {

    private static final String BUG = "ServiceProvisioningWorkflowRecoverableFailure";
    private static final String NEEDS_APPROVAL = "ServiceProvisioningWorkflowHumanInLoop";
    private static final String ADVANCED_VISIBILITY = "ServiceProvisioningWorkflowAdvancedVisibility";

    private static final Logger log = Workflow.getLogger(ServiceProvisioningWorkflowScenarios.class);

    private final SearchAttributeKey<String> WORKFLOW_STEP = SearchAttributeKey.forKeyword("Step");

    private final ServiceProvisioningActivities activities = Workflow.newActivityStub(
        ServiceProvisioningActivities.class,
        ServiceProvisioningActivities.activityOptions
    );

    private int progress = 0;
    private String serviceState = "starting";

    private int approvalTime = 60;
    private boolean approved = false;

    @Override
    public Object execute(EncodedValues args) {
        Workflow.registerListener(new ServiceProvisioningDynamicListenerImpl());
        ServiceProvisioningInput input = args.get(0, ServiceProvisioningInput.class);
        String type = Workflow.getInfo().getWorkflowType();
        log.info("Dynamic Service Provisioning workflow started, type = {}", type);
        String idempotencyKey = Workflow.randomUUID().toString();

        // Validate Service Request
        upsertStep("Validate Service Request");
        activities.validateServiceRequest(input);
        updateProgress(25, 1);

        // Reserve Resources
        upsertStep("Reserve Resources");
        activities.reserveResources(idempotencyKey, input);
        updateProgress(50, 3);

        if (BUG.equals(type)) {
            // Simulate bug
            throw new RuntimeException("Simulated bug - fix me!");
        }

        // Configure Service
        upsertStep("Configure Service");
        try {
            activities.configureService(idempotencyKey, input);
            updateProgress(75, 1);
        } catch (ActivityFailure e) {
            // if service configuration fails in an unrecoverable way, rollback the resource reservation and fail the workflow
            log.info("Service configuration failed with unrecoverable error, reverting resource reservation");

            // Undo Resource Reservation (rollback)
            activities.undoReservation(input);

            // return failure message
            String message = ((ApplicationFailure) e.getCause()).getOriginalMessage();
            throw ApplicationFailure.newNonRetryableFailure(message, "ServiceConfigurationFailed");
        }

        if (NEEDS_APPROVAL.equals(type)) {
            log.info(
                "Waiting for customer acknowledgment (modem plugged in, SIM activated) for workflow ID: {}",
                Workflow.getInfo().getWorkflowId()
            );
            updateProgress(60, 0, "waiting");

            // Wait for the approval for up to approvalTime
            boolean receivedApproval = Workflow.await(Duration.ofSeconds(approvalTime), () -> approved);

            // If the approval was not received within the timeout, fail the workflow
            if (!receivedApproval) {
                log.error(
                    "Customer acknowledgment not received within the {}-second time window: Failing the workflow.",
                    approvalTime
                );
                throw ApplicationFailure.newFailure(
                    "Customer acknowledgment not received within " + approvalTime + " seconds",
                    "ApprovalTimeout"
                );
            }
        }

        // Activate Service
        upsertStep("Activate Service");
        activities.activateService(input);
        updateProgress(100, 1, "finished");

        return new ServiceProvisioningOutput(input);
    }

    private void upsertStep(String step) {
        if (ADVANCED_VISIBILITY.equals(Workflow.getInfo().getWorkflowType())) {
            log.info("Advanced visibility .. {}", step);
            Workflow.upsertTypedSearchAttributes(WORKFLOW_STEP.valueSet(step));
        }
    }

    private void updateProgress(int progress, int sleep) {
        updateProgress(progress, sleep, "running");
    }

    private void updateProgress(int progress, int sleep, String serviceState) {
        if (sleep > 0) {
            Workflow.newTimer(Duration.ofSeconds(sleep),
                TimerOptions.newBuilder()
                    .setSummary("Processing time")
                    .build())
            .get();
        }
        this.serviceState = serviceState;
        this.progress = progress;
    }

    class ServiceProvisioningDynamicListenerImpl implements ServiceProvisioningMessages {

        @Override
        public ServiceProvisioningStatus queryServiceStatus() {
            return new ServiceProvisioningStatus(progress, serviceState, "", approvalTime);
        }

        @Override
        public void approveServiceSignal() {
            log.info("Approve Service Signal Received");

            if (serviceState.equals("waiting")) {
                approved = true;
            } else {
                log.info("Signal not applied: Service is not waiting for approval.");
            }
        }

        @Override
        public String approveServiceUpdate() {
            log.info("Approve Service Update Validated: Approving Service");
            approved = true;
            return "successfully approved service";
        }

        @Override
        public void approveServiceUpdateValidator() {
            log.info("Approve Service Update Received: Validating");
            if (approved) {
                throw new IllegalStateException("Validation Failed: Service already approved");
            }
            if (!serviceState.equals("waiting")) {
                throw new IllegalStateException("Validation Failed: Service doesn't require approval");
            }
        }
    }
}
