package com.learning.user_service.core.processor.paymentmethod.add;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.paymentmethod.add.AddPaymentMethodUsecase;
import com.learning.user_service.core.usecase.paymentmethod.add.AddPaymentMethodUsecaseRequest;
import com.learning.user_service.core.usecase.paymentmethod.add.AddPaymentMethodUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddPaymentMethodProcessor implements Processor<AddPaymentMethodProcessorRequest, AddPaymentMethodProcessorResponse> {

    private final AddPaymentMethodUsecase addPaymentMethodUsecase;

    public AddPaymentMethodProcessor(AddPaymentMethodUsecase addPaymentMethodUsecase) {
        this.addPaymentMethodUsecase = addPaymentMethodUsecase;
    }

    @Override
    public AddPaymentMethodProcessorResponse process(AddPaymentMethodProcessorRequest request) {
        AddPaymentMethodUsecaseResponse response = addPaymentMethodUsecase.execute(buildAddPaymentMethodUsecaseRequest(request));
        return Optional.of(response)
                .map(AddPaymentMethodProcessor::buildAddPaymentMethodProcessorResponse)
                .orElseThrow();
    }

    private static AddPaymentMethodUsecaseRequest buildAddPaymentMethodUsecaseRequest(AddPaymentMethodProcessorRequest request) {
        return AddPaymentMethodUsecaseRequest.builder()
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

    private static AddPaymentMethodProcessorResponse buildAddPaymentMethodProcessorResponse(AddPaymentMethodUsecaseResponse response) {
        return AddPaymentMethodProcessorResponse.builder()
                .method(response.getMethod())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
