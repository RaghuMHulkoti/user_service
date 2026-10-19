package com.learning.user_service.core.usecase.paymentmethod.makedefault;

import com.learning.user_service.core.service.paymentmethod.makedefault.MakePaymentMethodDefaultService;
import com.learning.user_service.core.service.paymentmethod.makedefault.MakePaymentMethodDefaultServiceRequest;
import com.learning.user_service.core.service.paymentmethod.makedefault.MakePaymentMethodDefaultServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MakePaymentMethodDefaultUsecase implements Usecase<MakePaymentMethodDefaultUsecaseRequest, MakePaymentMethodDefaultUsecaseResponse> {

    private final MakePaymentMethodDefaultService makePaymentMethodDefaultService;

    public MakePaymentMethodDefaultUsecase(MakePaymentMethodDefaultService makePaymentMethodDefaultService) {
        this.makePaymentMethodDefaultService = makePaymentMethodDefaultService;
    }

    @Override
    public MakePaymentMethodDefaultUsecaseResponse execute(MakePaymentMethodDefaultUsecaseRequest request) {
        MakePaymentMethodDefaultServiceResponse response = makePaymentMethodDefaultService.execute(buildMakePaymentMethodDefaultServiceRequest(request));
        return Optional.of(response)
                .map(MakePaymentMethodDefaultUsecase::buildMakePaymentMethodDefaultUsecaseResponse)
                .orElseThrow();
    }

    private static MakePaymentMethodDefaultServiceRequest buildMakePaymentMethodDefaultServiceRequest(MakePaymentMethodDefaultUsecaseRequest request) {
        return MakePaymentMethodDefaultServiceRequest.builder()
                .userId(request.getUserId())
                .methodId(request.getMethodId())
                .build();
    }

    private static MakePaymentMethodDefaultUsecaseResponse buildMakePaymentMethodDefaultUsecaseResponse(MakePaymentMethodDefaultServiceResponse response) {
        return MakePaymentMethodDefaultUsecaseResponse.builder()
                .method(response.getMethod())
                .build();
    }
}
