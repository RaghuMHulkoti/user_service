package com.learning.user_service.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestWebRequest(@NotBlank @Size(max = 255) String email) {
}
