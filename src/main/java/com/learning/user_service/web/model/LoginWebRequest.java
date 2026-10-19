package com.learning.user_service.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginWebRequest(@NotBlank @Size(max = 255) String email, @NotBlank @Size(max = 200) String password) {
}
