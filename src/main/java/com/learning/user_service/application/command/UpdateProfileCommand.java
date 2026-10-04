package com.learning.user_service.application.command;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Partial update: null fields are left unchanged. {@code version} enables optimistic locking. */
public record UpdateProfileCommand(
        @Size(min = 1, max = 80) String firstName,
        @Size(max = 80) String lastName,
        @Pattern(regexp = ValidationPatterns.E164_PHONE, message = "must be in E.164 format, e.g. +919876543210")
        String phone,
        @Pattern(regexp = ValidationPatterns.LOCALE, message = "must look like en-IN") String locale,
        Long version) {
}
