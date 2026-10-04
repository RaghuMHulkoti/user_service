package com.learning.user_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordCommand(@NotBlank @Size(max = 200) String currentPassword,
                                    @NotBlank @Size(max = 200) String newPassword) {
}
