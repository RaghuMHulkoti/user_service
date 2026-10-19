package com.learning.user_service.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserWebRequest(
        @NotBlank @Size(max = 255) @Pattern(regexp = ValidationPatterns.EMAIL, message = "must be a valid email address")
        String email,
        @NotBlank @Size(max = 200) String password,
        @NotBlank @Size(max = 80) String firstName,
        @Size(max = 80) String lastName,
        @Pattern(regexp = ValidationPatterns.E164_PHONE, message = "must be in E.164 format, e.g. +919876543210")
        String phone,
        @Pattern(regexp = ValidationPatterns.LOCALE, message = "must look like en-IN") String locale) {
}
