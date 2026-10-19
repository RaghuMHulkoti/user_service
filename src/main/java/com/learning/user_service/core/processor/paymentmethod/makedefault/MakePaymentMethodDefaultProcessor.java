package com.learning.user_service.core.processor.paymentmethod.makedefault;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.paymentmethod.makedefault.MakePaymentMethodDefaultUsecase;
import com.learning.user_service.core.usecase.paymentmethod.makedefault.MakePaymentMethodDefaultUsecaseRequest;
import com.learning.user_service.core.usecase.paymentmethod.makedefault.MakePaymentMethodDefaultUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MakePaymentMethodDefaultProcessor implements Processor<MakePaymentMethodDefaultProcessorRequest, MakePaymentMethodDefaultProcessorResponse> {

    private final MakePaymentMethodDefaultUsecase makePaymentMethodDefaultUsecase;

    public MakePaymentMethodDefaultProcessor(MakePaymentMethodDefaultUsecase makePaymentMethodDefaultUsecase) {
        this.makePaymentMethodDefaultUsecase = makePaymentMethodDefaultUsecase;
    }

    @Override
    public MakePaymentMethodDefaultProcessorResponse process(MakePaymentMethodDefaultProcessorRequest request) {
        MakePaymentMethodDefaultUsecaseResponse response = makePaymentMethodDefaultUsecase.execute(buildMakePaymentMethodDefaultUsecaseRequest(request));
        return Optional.of(response)
                .map(MakePaymentMethodDefaultProcessor::buildMakePaymentMethodDefaultProcessorResponse)
                .orElseThrow();
    }

    private static MakePaymentMethodDefaultUsecaseRequest buildMakePaymentMethodDefaultUsecaseRequest(MakePaymentMethodDefaultProcessorRequest request) {
        return MakePaymentMethodDefaultUsecaseRequest.builder()
                .userId(request.getUserId())
                .methodId(request.getMethodId())
                .build();
    }

    private static MakePaymentMethodDefaultProcessorResponse buildMakePaymentMethodDefaultProcessorResponse(MakePaymentMethodDefaultUsecaseResponse response) {
        return MakePaymentMethodDefaultProcessorResponse.builder()
                .method(response.getMethod())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
