package com.learning.user_service.core.service.paymentmethod.list;

import com.learning.user_service.core.service.ServiceResponse;
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
public class ListPaymentMethodsServiceResponse implements ServiceResponse {

    private List<PaymentMethodTokenEntity> methods;
}
