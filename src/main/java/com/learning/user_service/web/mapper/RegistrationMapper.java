package com.learning.user_service.web.mapper;

import com.learning.user_service.core.domain.RegistrationResult;
import com.learning.user_service.core.processor.auth.register.RegisterUserProcessorRequest;
import com.learning.user_service.core.processor.auth.register.RegisterUserProcessorResponse;
import com.learning.user_service.core.processor.auth.resendverification.ResendVerificationProcessorRequest;
import com.learning.user_service.core.processor.auth.verifyemail.VerifyEmailProcessorRequest;
import com.learning.user_service.web.model.RegisterUserWebRequest;
import com.learning.user_service.web.model.VerifyEmailWebRequest;

import java.util.UUID;

public final class RegistrationMapper {

    private RegistrationMapper() {
    }

    public static RegisterUserProcessorRequest toRegisterUserRequest(RegisterUserWebRequest request) {
        return RegisterUserProcessorRequest.builder()
                .email(request.email())
                .password(request.password())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .locale(request.locale())
                .build();
    }

    public static RegistrationResult mapRegisterUserResponse(RegisterUserProcessorResponse response) {
        return response.getRegistrationResult();
    }

    public static VerifyEmailProcessorRequest toVerifyEmailRequest(VerifyEmailWebRequest request) {
        return VerifyEmailProcessorRequest.builder()
                .token(request.token())
                .build();
    }

    public static ResendVerificationProcessorRequest toResendVerificationRequest(UUID userId) {
        return ResendVerificationProcessorRequest.builder()
                .userId(userId)
                .build();
    }
}
