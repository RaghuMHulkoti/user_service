package com.learning.user_service.core.processor.auth.passwordresetrequest;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.passwordresetrequest.RequestPasswordResetUsecase;
import com.learning.user_service.core.usecase.auth.passwordresetrequest.RequestPasswordResetUsecaseRequest;
import com.learning.user_service.core.usecase.auth.passwordresetrequest.RequestPasswordResetUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestPasswordResetProcessor implements Processor<RequestPasswordResetProcessorRequest, RequestPasswordResetProcessorResponse> {

    private final RequestPasswordResetUsecase requestPasswordResetUsecase;

    public RequestPasswordResetProcessor(RequestPasswordResetUsecase requestPasswordResetUsecase) {
        this.requestPasswordResetUsecase = requestPasswordResetUsecase;
    }

    @Override
    public RequestPasswordResetProcessorResponse process(RequestPasswordResetProcessorRequest request) {
        RequestPasswordResetUsecaseResponse response = requestPasswordResetUsecase.execute(buildUsecaseRequest(request));
        return Optional.of(response)
                .map(RequestPasswordResetProcessor::buildProcessorResponse)
                .orElseThrow();
    }

    private static RequestPasswordResetUsecaseRequest buildUsecaseRequest(RequestPasswordResetProcessorRequest request) {
        return RequestPasswordResetUsecaseRequest.builder()
                .email(request.getEmail())
                .build();
    }

    private static RequestPasswordResetProcessorResponse buildProcessorResponse(RequestPasswordResetUsecaseResponse response) {
        return RequestPasswordResetProcessorResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
