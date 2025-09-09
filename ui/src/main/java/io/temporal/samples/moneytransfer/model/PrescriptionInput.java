package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionInput {
    private String prescriptionId;
    private String patientId;
    private String medicationName;
    private int quantity;
    private String doctorId;
    private String pharmacyId;
    private String insuranceId;
}
