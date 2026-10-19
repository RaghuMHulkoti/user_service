package com.learning.user_service.core.service.paymentmethod.add;

import com.learning.user_service.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPaymentMethodServiceResponse implements ServiceResponse {

    private PaymentMethodTokenEntity method;
}
