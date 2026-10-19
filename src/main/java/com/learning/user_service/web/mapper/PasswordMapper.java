package com.learning.user_service.web.mapper;

import com.learning.user_service.core.processor.auth.passwordreset.ResetPasswordProcessorRequest;
import com.learning.user_service.core.processor.auth.passwordreset.ResetPasswordProcessorResponse;
import com.learning.user_service.core.processor.auth.passwordresetrequest.RequestPasswordResetProcessorRequest;
import com.learning.user_service.core.processor.auth.passwordresetrequest.RequestPasswordResetProcessorResponse;
import com.learning.user_service.core.processor.profile.changepassword.ChangePasswordProcessorRequest;
import com.learning.user_service.core.processor.profile.changepassword.ChangePasswordProcessorResponse;
import com.learning.user_service.web.model.ChangePasswordWebRequest;
import com.learning.user_service.web.model.PasswordResetRequestWebRequest;
import com.learning.user_service.web.model.PasswordResetWebRequest;

import java.util.UUID;

public final class PasswordMapper {

    private PasswordMapper() {
    }

    public static RequestPasswordResetProcessorRequest toRequestPasswordResetRequest(
            PasswordResetRequestWebRequest cmd) {
        return RequestPasswordResetProcessorRequest.builder()
                .email(cmd.email())
                .build();
    }

    public static void mapRequestPasswordResetResponse(RequestPasswordResetProcessorResponse response) {
    }

    public static ResetPasswordProcessorRequest toResetPasswordRequest(PasswordResetWebRequest cmd) {
        return ResetPasswordProcessorRequest.builder()
                .token(cmd.token())
                .newPassword(cmd.newPassword())
                .build();
    }

    public static void mapResetPasswordResponse(ResetPasswordProcessorResponse response) {
    }

    public static ChangePasswordProcessorRequest toChangePasswordRequest(UUID userId,
                                                                         ChangePasswordWebRequest cmd) {
        return ChangePasswordProcessorRequest.builder()
                .userId(userId)
                .currentPassword(cmd.currentPassword())
                .newPassword(cmd.newPassword())
                .build();
    }

    public static void mapChangePasswordResponse(ChangePasswordProcessorResponse response) {
    }
}
