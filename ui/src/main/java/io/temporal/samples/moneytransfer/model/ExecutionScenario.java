package io.temporal.samples.moneytransfer.model;

public enum ExecutionScenario {
    HAPPY_PATH("PharmacyFulfillmentWorkflow"),
    ADVANCED_VISIBILITY("PharmacyFulfillmentWorkflowAdvancedVisibility"),
    HUMAN_IN_LOOP("PharmacyFulfillmentWorkflowHumanInLoop"),
    PHARMACY_SYSTEM_DOWNTIME("PharmacyFulfillmentWorkflowAPIDowntime"),
    BUG_IN_WORKFLOW("PharmacyFulfillmentWorkflowRecoverableFailure"),
    INVALID_PRESCRIPTION("PharmacyFulfillmentWorkflowInvalidPrescription");

    private final String workflowType;

    ExecutionScenario(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getWorkflowType() {
        return this.workflowType;
    }
}
