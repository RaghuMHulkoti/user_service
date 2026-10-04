package com.learning.user_service.application.query;

import com.learning.user_service.domain.model.UserStatus;

import java.util.UUID;

public record RegistrationResult(UUID userId, String email, UserStatus status, boolean verificationEmailSent) {
}
