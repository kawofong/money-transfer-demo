package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvisioningStatus {

    private int progressPercentage;
    private String serviceState;
    private String workflowStatus;
    private int approvalTime;
}
