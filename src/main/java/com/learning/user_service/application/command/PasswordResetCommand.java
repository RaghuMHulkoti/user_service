package com.learning.user_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetCommand(@NotBlank @Size(max = 200) String token,
                                   @NotBlank @Size(max = 200) String newPassword) {
}
