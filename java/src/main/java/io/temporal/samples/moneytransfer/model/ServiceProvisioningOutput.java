package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvisioningOutput {
    private String customerId;
    private String serviceType;
    private String serviceLevel;
    private String status;
    private String activationDate;

    public ServiceProvisioningOutput(ServiceProvisioningInput input) {
        this.customerId = input.getCustomerId();
        this.serviceType = input.getServiceType();
        this.serviceLevel = input.getServiceLevel();
        this.status = "ACTIVATED";
        this.activationDate = java.time.LocalDateTime.now().toString();
    }
}
