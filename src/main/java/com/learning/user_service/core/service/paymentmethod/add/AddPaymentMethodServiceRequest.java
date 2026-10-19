package com.learning.user_service.core.service.paymentmethod.add;

import com.learning.user_service.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.model.PaymentProvider;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPaymentMethodServiceRequest implements ServiceRequest {

    private UUID userId;
    private PaymentProvider provider;
    private String providerToken;
    private String brand;
    private String last4;
    private Short expMonth;
    private Short expYear;
    private Boolean isDefault;
}
