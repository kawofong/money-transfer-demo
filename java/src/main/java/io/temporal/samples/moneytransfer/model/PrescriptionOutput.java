package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionOutput {

    private AdjudicationResponse adjudicationResponse;
    private String fulfillmentStatus;
    private String pickupLocation;
}
