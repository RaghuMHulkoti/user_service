package com.learning.user_service.core.usecase.auth.passwordreset;

import com.learning.user_service.core.service.auth.passwordreset.ResetPasswordService;
import com.learning.user_service.core.service.auth.passwordreset.ResetPasswordServiceRequest;
import com.learning.user_service.core.service.auth.passwordreset.ResetPasswordServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResetPasswordUsecase implements Usecase<ResetPasswordUsecaseRequest, ResetPasswordUsecaseResponse> {

    private final ResetPasswordService resetPasswordService;

    public ResetPasswordUsecase(ResetPasswordService resetPasswordService) {
        this.resetPasswordService = resetPasswordService;
    }

    @Override
    public ResetPasswordUsecaseResponse execute(ResetPasswordUsecaseRequest request) {
        ResetPasswordServiceResponse response = resetPasswordService.execute(buildServiceRequest(request));
        return Optional.of(response)
                .map(ResetPasswordUsecase::buildUsecaseResponse)
                .orElseThrow();
    }

    private static ResetPasswordServiceRequest buildServiceRequest(ResetPasswordUsecaseRequest request) {
        return ResetPasswordServiceRequest.builder()
                .token(request.getToken())
                .newPassword(request.getNewPassword())
                .build();
    }

    private static ResetPasswordUsecaseResponse buildUsecaseResponse(ResetPasswordServiceResponse response) {
        return ResetPasswordUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
