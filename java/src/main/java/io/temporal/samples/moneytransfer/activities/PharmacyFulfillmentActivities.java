package io.temporal.samples.moneytransfer.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.samples.moneytransfer.model.AdjudicationResponse;
import io.temporal.samples.moneytransfer.model.PrescriptionInput;

import java.time.Duration;

@ActivityInterface
public interface PharmacyFulfillmentActivities {
    ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(
                    RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setBackoffCoefficient(2)
                            .setMaximumInterval(Duration.ofSeconds(30))
                            .build()
            )
            .build();

    @ActivityMethod
    String verifyOrder(PrescriptionInput input);

    @ActivityMethod
    String loadPrescriptionToPharmacySystem(String idempotencyKey, PrescriptionInput input, String type);

    @ActivityMethod
    AdjudicationResponse adjudication(String idempotencyKey, PrescriptionInput input, String type);

    @ActivityMethod
    String notifyCustomer(PrescriptionInput input);

    @ActivityMethod
    boolean reversePharmacySystemLoad(PrescriptionInput input);
}
