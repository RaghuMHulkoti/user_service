package com.learning.user_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailCommand(@NotBlank @Size(max = 200) String token) {
}
