package io.temporal.samples.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjudicationResponse {

    @JsonProperty("adjudicationId")
    private String adjudicationId;
    private String insuranceCoverage;
    private double copayAmount;
    private String approvalStatus;
}
