package com.learning.user_service.core.processor.paymentmethod.list;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPaymentMethodsProcessorResponse implements ProcessorResponse {

    private List<PaymentMethodTokenEntity> methods;
    private StatusCode statusCode;
}
