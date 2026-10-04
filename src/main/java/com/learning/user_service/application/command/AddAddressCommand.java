package com.learning.user_service.application.command;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddAddressCommand(
        @Size(max = 40) String label,
        @NotBlank @Size(max = 200) String line1,
        @Size(max = 200) String line2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Pattern(regexp = ValidationPatterns.COUNTRY, message = "must be an ISO 3166-1 alpha-2 code, e.g. IN")
        String country,
        Boolean isDefault,
        @DecimalMin("-90") @DecimalMax("90") BigDecimal geoLat,
        @DecimalMin("-180") @DecimalMax("180") BigDecimal geoLng) {
}
