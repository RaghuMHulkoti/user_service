package com.learning.user_service.core.usecase.paymentmethod.add;

import com.learning.user_service.core.service.paymentmethod.add.AddPaymentMethodService;
import com.learning.user_service.core.service.paymentmethod.add.AddPaymentMethodServiceRequest;
import com.learning.user_service.core.service.paymentmethod.add.AddPaymentMethodServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddPaymentMethodUsecase implements Usecase<AddPaymentMethodUsecaseRequest, AddPaymentMethodUsecaseResponse> {

    private final AddPaymentMethodService addPaymentMethodService;

    public AddPaymentMethodUsecase(AddPaymentMethodService addPaymentMethodService) {
        this.addPaymentMethodService = addPaymentMethodService;
    }

    @Override
    public AddPaymentMethodUsecaseResponse execute(AddPaymentMethodUsecaseRequest request) {
        AddPaymentMethodServiceResponse response = addPaymentMethodService.execute(buildAddPaymentMethodServiceRequest(request));
        return Optional.of(response)
                .map(AddPaymentMethodUsecase::buildAddPaymentMethodUsecaseResponse)
                .orElseThrow();
    }

    private static AddPaymentMethodServiceRequest buildAddPaymentMethodServiceRequest(AddPaymentMethodUsecaseRequest request) {
        return AddPaymentMethodServiceRequest.builder()
                .userId(request.getUserId())
                .provider(request.getProvider())
                .providerToken(request.getProviderToken())
                .brand(request.getBrand())
                .last4(request.getLast4())
                .expMonth(request.getExpMonth())
                .expYear(request.getExpYear())
                .isDefault(request.getIsDefault())
                .build();
    }

    private static AddPaymentMethodUsecaseResponse buildAddPaymentMethodUsecaseResponse(AddPaymentMethodServiceResponse response) {
        return AddPaymentMethodUsecaseResponse.builder()
                .method(response.getMethod())
                .build();
    }
}
