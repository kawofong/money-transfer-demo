package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvisioningInput {
    private String customerId;
    private String serviceType;
    private String serviceLevel;

    public String getCustomerId() {
        return customerId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }
}
