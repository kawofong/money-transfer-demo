package io.temporal.samples.moneytransfer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUXParameters {
    private String customerId;
    private String serviceType;
    private String serviceLevel;
    private ExecutionScenario scenario;

    public ServiceProvisioningInput toServiceProvisioningInput() {
        return new ServiceProvisioningInput(
            customerId,
            serviceType,
            serviceLevel
        );
    }

}
