package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjudicationResponse {
    private String adjudicationId;
    private String insuranceCoverage;
    private double copayAmount;
    private String approvalStatus;
}
