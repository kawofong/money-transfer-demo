package io.temporal.samples.moneytransfer.model;

public enum ExecutionScenario {
    // HAPPY_PATH("AccountTransferWorkflow"),
    // ADVANCED_VISIBILITY("AccountTransferWorkflowAdvancedVisibility"),
    // HUMAN_IN_LOOP("AccountTransferWorkflowHumanInLoop"),
    // API_DOWNTIME("AccountTransferWorkflowAPIDowntime"),
    // BUG_IN_WORKFLOW("AccountTransferWorkflowRecoverableFailure"),
    // INVALID_ACCOUNT("AccountTransferWorkflowInvalidAccount");
    HAPPY_PATH("ServiceProvisioningWorkflow"),
    ADVANCED_VISIBILITY("ServiceProvisioningWorkflowAdvancedVisibility"),
    HUMAN_IN_LOOP("ServiceProvisioningWorkflowHumanInLoop"),
    API_DOWNTIME("ServiceProvisioningWorkflowAPIDowntime"),
    BUG_IN_WORKFLOW("ServiceProvisioningWorkflowRecoverableFailure"),
    INVALID_ACCOUNT("ServiceProvisioningWorkflowInvalidAccount");

    private final String workflowType;

    ExecutionScenario(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getWorkflowType() {
        return this.workflowType;
    }
}
