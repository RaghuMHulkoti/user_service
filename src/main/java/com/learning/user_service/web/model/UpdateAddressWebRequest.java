package com.learning.user_service.web.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Partial update: null fields are left unchanged. */
public record UpdateAddressWebRequest(
        @Size(max = 40) String label,
        @Size(min = 1, max = 200) String line1,
        @Size(max = 200) String line2,
        @Size(min = 1, max = 100) String city,
        @Size(min = 1, max = 100) String state,
        @Size(min = 1, max = 20) String postalCode,
        @Pattern(regexp = ValidationPatterns.COUNTRY, message = "must be an ISO 3166-1 alpha-2 code, e.g. IN")
        String country,
        Boolean isDefault,
        @DecimalMin("-90") @DecimalMax("90") BigDecimal geoLat,
        @DecimalMin("-180") @DecimalMax("180") BigDecimal geoLng,
        Long version) {
}
