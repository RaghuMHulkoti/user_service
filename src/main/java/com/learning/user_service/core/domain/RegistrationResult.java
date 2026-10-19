package com.learning.user_service.core.domain;

import com.learning.user_service.core.domain.model.UserStatus;

import java.util.UUID;

public record RegistrationResult(UUID userId, String email, UserStatus status, boolean verificationEmailSent) {
}
