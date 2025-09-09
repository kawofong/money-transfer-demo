package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UXParameters {
    private String prescriptionId;
    private String patientId;
    private String medicationName;
    private int quantity;
    private String doctorId;
    private String pharmacyId;
    private String insuranceId;
    private ExecutionScenario scenario;

    public PrescriptionInput toPrescriptionInput() {
        return new PrescriptionInput(this.prescriptionId, this.patientId, this.medicationName,
                                   this.quantity, this.doctorId, this.pharmacyId, this.insuranceId);
    }
}
