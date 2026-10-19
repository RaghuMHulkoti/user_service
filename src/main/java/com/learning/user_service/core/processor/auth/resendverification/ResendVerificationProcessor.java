package com.learning.user_service.core.processor.auth.resendverification;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.resendverification.ResendVerificationUsecase;
import com.learning.user_service.core.usecase.auth.resendverification.ResendVerificationUsecaseRequest;
import com.learning.user_service.core.usecase.auth.resendverification.ResendVerificationUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResendVerificationProcessor implements Processor<ResendVerificationProcessorRequest, ResendVerificationProcessorResponse> {

    private final ResendVerificationUsecase resendVerificationUsecase;

    public ResendVerificationProcessor(ResendVerificationUsecase resendVerificationUsecase) {
        this.resendVerificationUsecase = resendVerificationUsecase;
    }

    @Override
    public ResendVerificationProcessorResponse process(ResendVerificationProcessorRequest request) {
        ResendVerificationUsecaseResponse usecaseResponse = resendVerificationUsecase.execute(buildResendVerificationUsecaseRequest(request));
        return Optional.of(usecaseResponse)
                .map(ResendVerificationProcessor::buildResendVerificationProcessorResponse)
                .orElseThrow();
    }

    private static ResendVerificationUsecaseRequest buildResendVerificationUsecaseRequest(ResendVerificationProcessorRequest request) {
        return ResendVerificationUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ResendVerificationProcessorResponse buildResendVerificationProcessorResponse(ResendVerificationUsecaseResponse response) {
        return ResendVerificationProcessorResponse.builder()
                .success(response.isSuccess())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
