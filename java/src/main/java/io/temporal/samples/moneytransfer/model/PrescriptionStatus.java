package io.temporal.samples.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("adjudicationResult")
    private AdjudicationResponse adjudicationResponse;

    private int approvalTime;
}
