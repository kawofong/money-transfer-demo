package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.activities.ServiceProvisioningActivities;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningInput;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningOutput;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningStatus;
import io.temporal.workflow.TimerOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class ServiceProvisioningWorkflowImpl implements ServiceProvisioningWorkflow {

    private static final Logger log = Workflow.getLogger(ServiceProvisioningWorkflowImpl.class);

    private final ServiceProvisioningActivities activities = Workflow.newActivityStub(
            ServiceProvisioningActivities.class,
            ServiceProvisioningActivities.activityOptions
    );

    private int progress = 0;
    private String serviceState = "starting";

    @Override
    public ServiceProvisioningOutput provisionService(ServiceProvisioningInput input) {
        String workflowType = Workflow.getInfo().getWorkflowType();
        log.info("Service Provisioning workflow started, type = {}", workflowType);
        String idempotencyKey = Workflow.randomUUID().toString();

        // Validate Service Request
        activities.validateServiceRequest(input);
        updateProgress(25, 1);

        // Reserve Resources
        activities.reserveResources(idempotencyKey, input);
        updateProgress(50, 3);

        // Configure Service
        activities.configureService(idempotencyKey, input);
        updateProgress(75, 1);

        // Activate Service
        activities.activateService(input);
        updateProgress(100, 1, "finished");

        return new ServiceProvisioningOutput(input);
    }

    @Override
    public ServiceProvisioningStatus queryServiceStatus() {
        return new ServiceProvisioningStatus(progress, serviceState, "", 0);
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
}
