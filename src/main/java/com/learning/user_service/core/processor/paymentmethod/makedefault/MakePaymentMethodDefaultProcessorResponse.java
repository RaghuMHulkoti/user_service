package com.learning.user_service.core.processor.paymentmethod.makedefault;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakePaymentMethodDefaultProcessorResponse implements ProcessorResponse {

    private PaymentMethodTokenEntity method;
    private StatusCode statusCode;
}
