package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionStatus {
    private int progressPercentage;
    private String prescriptionState;
    private String workflowStatus;
    private AdjudicationResponse adjudicationResult;
    private int approvalTime;
}
