package com.learning.user_service.core.processor.auth.passwordreset;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.passwordreset.ResetPasswordUsecase;
import com.learning.user_service.core.usecase.auth.passwordreset.ResetPasswordUsecaseRequest;
import com.learning.user_service.core.usecase.auth.passwordreset.ResetPasswordUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ResetPasswordProcessor implements Processor<ResetPasswordProcessorRequest, ResetPasswordProcessorResponse> {

    private final ResetPasswordUsecase resetPasswordUsecase;

    public ResetPasswordProcessor(ResetPasswordUsecase resetPasswordUsecase) {
        this.resetPasswordUsecase = resetPasswordUsecase;
    }

    @Override
    public ResetPasswordProcessorResponse process(ResetPasswordProcessorRequest request) {
        ResetPasswordUsecaseResponse response = resetPasswordUsecase.execute(buildUsecaseRequest(request));
        return Optional.of(response)
                .map(ResetPasswordProcessor::buildProcessorResponse)
                .orElseThrow();
    }

    private static ResetPasswordUsecaseRequest buildUsecaseRequest(ResetPasswordProcessorRequest request) {
        return ResetPasswordUsecaseRequest.builder()
                .token(request.getToken())
                .newPassword(request.getNewPassword())
                .build();
    }

    private static ResetPasswordProcessorResponse buildProcessorResponse(ResetPasswordUsecaseResponse response) {
        return ResetPasswordProcessorResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
