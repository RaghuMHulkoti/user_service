package com.learning.user_service.core.usecase.auth.verifyemail;

import com.learning.user_service.core.usecase.Usecase;
import com.learning.user_service.core.service.auth.verifyemail.VerifyEmailService;
import com.learning.user_service.core.service.auth.verifyemail.VerifyEmailServiceRequest;
import com.learning.user_service.core.service.auth.verifyemail.VerifyEmailServiceResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VerifyEmailUsecase implements Usecase<VerifyEmailUsecaseRequest, VerifyEmailUsecaseResponse> {

    private final VerifyEmailService verifyEmailService;

    public VerifyEmailUsecase(VerifyEmailService verifyEmailService) {
        this.verifyEmailService = verifyEmailService;
    }

    @Override
    public VerifyEmailUsecaseResponse execute(VerifyEmailUsecaseRequest request) {
        VerifyEmailServiceResponse serviceResponse = verifyEmailService.execute(buildVerifyEmailServiceRequest(request));
        return Optional.of(serviceResponse)
                .map(VerifyEmailUsecase::buildVerifyEmailUsecaseResponse)
                .orElseThrow();
    }

    private static VerifyEmailServiceRequest buildVerifyEmailServiceRequest(VerifyEmailUsecaseRequest request) {
        return VerifyEmailServiceRequest.builder()
                .token(request.getToken())
                .build();
    }

    private static VerifyEmailUsecaseResponse buildVerifyEmailUsecaseResponse(VerifyEmailServiceResponse response) {
        return VerifyEmailUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
