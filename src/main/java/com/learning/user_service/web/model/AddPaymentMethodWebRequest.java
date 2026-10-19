package com.learning.user_service.web.model;

import com.learning.user_service.core.domain.model.PaymentProvider;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A token the client got from the PSP (Stripe {@code pm_...}, Razorpay {@code token_...}).
 * Raw card numbers are rejected: the service stays out of PCI scope (TDD §8.2).
 */
public record AddPaymentMethodWebRequest(
        @NotNull PaymentProvider provider,
        @NotBlank @Size(max = 255)
        @Pattern(regexp = "^[A-Za-z0-9_\\-]{6,255}$", message = "must be a PSP token such as pm_...")
        String providerToken,
        @Size(max = 20) String brand,
        @Pattern(regexp = "^\\d{4}$", message = "must be 4 digits") String last4,
        @Min(1) @Max(12) Short expMonth,
        @Min(2000) @Max(2100) Short expYear,
        Boolean isDefault) {
}
