package com.learning.user_service.core.usecase.paymentmethod.makedefault;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakePaymentMethodDefaultUsecaseResponse implements UsecaseResponse {

    private PaymentMethodTokenEntity method;
}
