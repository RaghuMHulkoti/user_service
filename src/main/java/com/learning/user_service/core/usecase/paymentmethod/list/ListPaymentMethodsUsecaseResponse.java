package com.learning.user_service.core.usecase.paymentmethod.list;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPaymentMethodsUsecaseResponse implements UsecaseResponse {

    private List<PaymentMethodTokenEntity> methods;
}
