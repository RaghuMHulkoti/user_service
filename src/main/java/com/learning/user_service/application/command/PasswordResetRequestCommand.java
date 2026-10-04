package com.learning.user_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestCommand(@NotBlank @Size(max = 255) String email) {
}
