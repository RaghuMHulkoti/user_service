package com.learning.user_service.core.service.paymentmethod.list;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.gateway.PaymentMethodGateway;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListPaymentMethodsService implements Service<ListPaymentMethodsServiceRequest, ListPaymentMethodsServiceResponse> {

    private final PaymentMethodGateway paymentMethodGateway;
    private final UserAccess userAccess;

    public ListPaymentMethodsService(PaymentMethodGateway paymentMethodGateway, UserAccess userAccess) {
        this.paymentMethodGateway = paymentMethodGateway;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional(readOnly = true)
    public ListPaymentMethodsServiceResponse execute(ListPaymentMethodsServiceRequest request) {
        userAccess.loadUsable(request.getUserId());
        return ListPaymentMethodsServiceResponse.builder()
                .methods(paymentMethodGateway.findByUserId(request.getUserId()))
                .build();
    }
}
