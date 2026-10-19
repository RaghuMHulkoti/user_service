package com.learning.user_service.core.processor.profile.changepassword;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.profile.changepassword.ChangePasswordUsecase;
import com.learning.user_service.core.usecase.profile.changepassword.ChangePasswordUsecaseRequest;
import com.learning.user_service.core.usecase.profile.changepassword.ChangePasswordUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChangePasswordProcessor implements Processor<ChangePasswordProcessorRequest, ChangePasswordProcessorResponse> {

    private final ChangePasswordUsecase changePasswordUsecase;

    public ChangePasswordProcessor(ChangePasswordUsecase changePasswordUsecase) {
        this.changePasswordUsecase = changePasswordUsecase;
    }

    @Override
    public ChangePasswordProcessorResponse process(ChangePasswordProcessorRequest request) {
        ChangePasswordUsecaseResponse response = changePasswordUsecase.execute(buildUsecaseRequest(request));
        return Optional.of(response)
                .map(ChangePasswordProcessor::buildProcessorResponse)
                .orElseThrow();
    }

    private static ChangePasswordUsecaseRequest buildUsecaseRequest(ChangePasswordProcessorRequest request) {
        return ChangePasswordUsecaseRequest.builder()
                .userId(request.getUserId())
                .currentPassword(request.getCurrentPassword())
                .newPassword(request.getNewPassword())
                .build();
    }

    private static ChangePasswordProcessorResponse buildProcessorResponse(ChangePasswordUsecaseResponse response) {
        return ChangePasswordProcessorResponse.builder()
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
