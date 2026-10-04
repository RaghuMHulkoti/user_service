package com.learning.user_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginCommand(@NotBlank @Size(max = 255) String email, @NotBlank @Size(max = 200) String password) {
}
