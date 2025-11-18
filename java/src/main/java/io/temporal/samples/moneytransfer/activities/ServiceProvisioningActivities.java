package io.temporal.samples.moneytransfer.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningInput;

import java.time.Duration;

@ActivityInterface
public interface ServiceProvisioningActivities {
    ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(
                    RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setBackoffCoefficient(2)
                            .setMaximumInterval(Duration.ofSeconds(30))
                            .setMaximumAttempts(5)
                            .build()
            )
            .build();

    @ActivityMethod
    String validateServiceRequest(ServiceProvisioningInput input);

    @ActivityMethod
    String reserveResources(String idempotencyKey, ServiceProvisioningInput input);

    @ActivityMethod
    String configureService(String idempotencyKey, ServiceProvisioningInput input);

    @ActivityMethod
    String activateService(ServiceProvisioningInput input);

    @ActivityMethod
    boolean undoReservation(ServiceProvisioningInput input);
}
