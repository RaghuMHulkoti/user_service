package com.learning.user_service.core.service.paymentmethod.makedefault;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.gateway.PaymentMethodGateway;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MakePaymentMethodDefaultService implements Service<MakePaymentMethodDefaultServiceRequest, MakePaymentMethodDefaultServiceResponse> {

    private final PaymentMethodGateway paymentMethodGateway;
    private final UserAccess userAccess;

    public MakePaymentMethodDefaultService(PaymentMethodGateway paymentMethodGateway, UserAccess userAccess) {
        this.paymentMethodGateway = paymentMethodGateway;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional
    public MakePaymentMethodDefaultServiceResponse execute(MakePaymentMethodDefaultServiceRequest request) {
        UUID userId = request.getUserId();
        UUID methodId = request.getMethodId();
        userAccess.loadUsable(userId);
        findOwned(userId, methodId);
        paymentMethodGateway.clearDefault(userId);
        PaymentMethodTokenEntity method = findOwned(userId, methodId);
        method.setDefaultMethod(true);
        return MakePaymentMethodDefaultServiceResponse.builder()
                .method(paymentMethodGateway.saveAndFlush(method))
                .build();
    }

    private PaymentMethodTokenEntity findOwned(UUID userId, UUID methodId) {
        return paymentMethodGateway.findByIdAndUserId(methodId, userId)
                .orElseThrow(() -> ApiException.notFound("Payment method"));
    }
}
