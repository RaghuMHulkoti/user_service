package com.learning.user_service.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordWebRequest(@NotBlank @Size(max = 200) String currentPassword,
                                    @NotBlank @Size(max = 200) String newPassword) {
}
