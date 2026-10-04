package com.learning.user_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenCommand(@NotBlank @Size(max = 200) String refreshToken) {
}
