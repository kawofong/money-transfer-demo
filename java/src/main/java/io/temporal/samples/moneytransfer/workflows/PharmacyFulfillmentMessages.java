package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.model.PrescriptionStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.UpdateValidatorMethod;

public interface PharmacyFulfillmentMessages {
    @QueryMethod(name = "prescriptionStatus")
    PrescriptionStatus queryPrescriptionStatus();

    @SignalMethod(name = "approvePrescription")
    void approvePrescriptionSignal();

    @UpdateMethod
    String approvePrescriptionUpdate();

    @UpdateValidatorMethod(updateName = "approvePrescriptionUpdate")
    void approvePrescriptionUpdateValidator();
}
