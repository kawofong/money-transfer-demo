package io.temporal.samples.moneytransfer.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.samples.moneytransfer.model.DepositResponse;
import io.temporal.samples.moneytransfer.model.TransferInput;

import java.time.Duration;

@ActivityInterface
public interface AccountTransferActivities {
    ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(5))
            .setRetryOptions(
                    RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setBackoffCoefficient(2)
                            .setMaximumInterval(Duration.ofSeconds(30))
                            .build()
            )
            .build();

    @ActivityMethod
    String validateServiceRequest(TransferInput input);

    @ActivityMethod
    String reserveResources(String idempotencyKey, float amount, String type);

    @ActivityMethod
    DepositResponse configureService(String idempotencyKey, float amount, String type);

    @ActivityMethod
    String activateService(TransferInput input);

    @ActivityMethod
    boolean undoReservation(float amount);
}
