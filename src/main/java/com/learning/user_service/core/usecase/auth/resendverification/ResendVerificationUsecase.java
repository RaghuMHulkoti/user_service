package com.learning.user_service.core.usecase.auth.resendverification;

import com.learning.user_service.core.usecase.Usecase;
import com.learning.user_service.core.service.auth.resendverification.ResendVerificationService;
import com.learning.user_service.core.service.auth.resendverification.ResendVerificationServiceRequest;
import com.learning.user_service.core.service.auth.resendverification.ResendVerificationServiceResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResendVerificationUsecase implements Usecase<ResendVerificationUsecaseRequest, ResendVerificationUsecaseResponse> {

    private final ResendVerificationService resendVerificationService;

    public ResendVerificationUsecase(ResendVerificationService resendVerificationService) {
        this.resendVerificationService = resendVerificationService;
    }

    @Override
    public ResendVerificationUsecaseResponse execute(ResendVerificationUsecaseRequest request) {
        ResendVerificationServiceResponse serviceResponse = resendVerificationService.execute(buildResendVerificationServiceRequest(request));
        return Optional.of(serviceResponse)
                .map(ResendVerificationUsecase::buildResendVerificationUsecaseResponse)
                .orElseThrow();
    }

    private static ResendVerificationServiceRequest buildResendVerificationServiceRequest(ResendVerificationUsecaseRequest request) {
        return ResendVerificationServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ResendVerificationUsecaseResponse buildResendVerificationUsecaseResponse(ResendVerificationServiceResponse response) {
        return ResendVerificationUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
