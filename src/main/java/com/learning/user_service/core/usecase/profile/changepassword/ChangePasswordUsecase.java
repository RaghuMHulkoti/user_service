package com.learning.user_service.core.usecase.profile.changepassword;

import com.learning.user_service.core.service.profile.changepassword.ChangePasswordService;
import com.learning.user_service.core.service.profile.changepassword.ChangePasswordServiceRequest;
import com.learning.user_service.core.service.profile.changepassword.ChangePasswordServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChangePasswordUsecase implements Usecase<ChangePasswordUsecaseRequest, ChangePasswordUsecaseResponse> {

    private final ChangePasswordService changePasswordService;

    public ChangePasswordUsecase(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    @Override
    public ChangePasswordUsecaseResponse execute(ChangePasswordUsecaseRequest request) {
        ChangePasswordServiceResponse response = changePasswordService.execute(buildServiceRequest(request));
        return Optional.of(response)
                .map(ChangePasswordUsecase::buildUsecaseResponse)
                .orElseThrow();
    }

    private static ChangePasswordServiceRequest buildServiceRequest(ChangePasswordUsecaseRequest request) {
        return ChangePasswordServiceRequest.builder()
                .userId(request.getUserId())
                .currentPassword(request.getCurrentPassword())
                .newPassword(request.getNewPassword())
                .build();
    }

    private static ChangePasswordUsecaseResponse buildUsecaseResponse(ChangePasswordServiceResponse response) {
        return ChangePasswordUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
