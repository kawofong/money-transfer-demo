package io.temporal.samples.moneytransfer.workflows;

import io.temporal.samples.moneytransfer.model.ServiceProvisioningStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.UpdateValidatorMethod;

public interface ServiceProvisioningMessages {
    @QueryMethod(name = "serviceStatus")
    ServiceProvisioningStatus queryServiceStatus();

    @SignalMethod(name = "approveService")
    void approveServiceSignal();

    @UpdateMethod
    String approveServiceUpdate();

    @UpdateValidatorMethod(updateName = "approveServiceUpdate")
    void approveServiceUpdateValidator();
}
