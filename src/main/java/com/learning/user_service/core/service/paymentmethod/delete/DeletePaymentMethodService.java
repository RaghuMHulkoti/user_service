package com.learning.user_service.core.service.paymentmethod.delete;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.gateway.PaymentMethodGateway;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeletePaymentMethodService implements Service<DeletePaymentMethodServiceRequest, DeletePaymentMethodServiceResponse> {

    private final PaymentMethodGateway paymentMethodGateway;
    private final UserAccess userAccess;

    public DeletePaymentMethodService(PaymentMethodGateway paymentMethodGateway, UserAccess userAccess) {
        this.paymentMethodGateway = paymentMethodGateway;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional
    public DeletePaymentMethodServiceResponse execute(DeletePaymentMethodServiceRequest request) {
        UUID userId = request.getUserId();
        UUID methodId = request.getMethodId();
        userAccess.loadUsable(userId);
        PaymentMethodTokenEntity method = findOwned(userId, methodId);
        boolean wasDefault = method.isDefaultMethod();
        paymentMethodGateway.delete(method);
        paymentMethodGateway.flush();
        if (wasDefault) {
            paymentMethodGateway.findFirstByUserId(userId).ifPresent(next -> next.setDefaultMethod(true));
        }
        return DeletePaymentMethodServiceResponse.builder().success(true).build();
    }

    private PaymentMethodTokenEntity findOwned(UUID userId, UUID methodId) {
        return paymentMethodGateway.findByIdAndUserId(methodId, userId)
                .orElseThrow(() -> ApiException.notFound("Payment method"));
    }
}
