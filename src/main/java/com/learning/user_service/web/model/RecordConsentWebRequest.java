package com.learning.user_service.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** e.g. {@code { "kind": "MARKETING_EMAIL", "granted": true }}. */
public record RecordConsentWebRequest(
        @NotBlank @Pattern(regexp = ValidationPatterns.CONSENT_KIND, message = "must be UPPER_SNAKE_CASE")
        String kind,
        @NotNull Boolean granted) {
}
