package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.model.ServiceProvisioningInput;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningOutput;
import io.temporal.samples.moneytransfer.model.ServiceProvisioningStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ServiceProvisioningWorkflow {

    @WorkflowMethod
    ServiceProvisioningOutput provisionService(ServiceProvisioningInput input);

    @QueryMethod(name = "serviceStatus")
    ServiceProvisioningStatus queryServiceStatus();
}
