package io.temporal.samples.moneytransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.temporal.samples.moneytransfer.model.PrescriptionInput;
import io.temporal.samples.moneytransfer.model.PrescriptionOutput;
import io.temporal.samples.moneytransfer.model.PrescriptionStatus;
import io.temporal.workflow.*;

@WorkflowInterface
public interface PharmacyFulfillmentWorkflow {
    @WorkflowMethod
    PrescriptionOutput fulfillPrescription(PrescriptionInput params);

    @QueryMethod(name = "prescriptionStatus")
    PrescriptionStatus getStateQuery() throws JsonProcessingException;

    @SignalMethod(name = "approvePrescription")
    void approvePrescription();

    @UpdateMethod
    String approvePrescriptionUpdate();

    @UpdateValidatorMethod(updateName = "approvePrescriptionUpdate")
    void approvePrescriptionUpdateValidator();
}
