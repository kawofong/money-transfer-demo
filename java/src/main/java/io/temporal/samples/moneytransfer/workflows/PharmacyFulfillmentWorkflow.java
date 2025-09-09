package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.model.PrescriptionInput;
import io.temporal.samples.moneytransfer.model.PrescriptionOutput;
import io.temporal.samples.moneytransfer.model.PrescriptionStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PharmacyFulfillmentWorkflow {
    @WorkflowMethod
    PrescriptionOutput fulfillPrescription(PrescriptionInput input);

    @QueryMethod(name = "prescriptionStatus")
    PrescriptionStatus queryPrescriptionStatus();
}
