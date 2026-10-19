package com.learning.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Security security,
        RateLimit rateLimit,
        Tokens tokens,
        Idempotency idempotency,
        Events events,
        Outbox outbox,
        Dev dev) {

    public record Security(
            String issuer,
            String tenantId,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            int bcryptStrength,
            String privateKeyLocation,
            String publicKeyLocation,
            String keyId,
            String internalServiceToken,
            int maxFailedLogins,
            Duration lockoutDuration) {
    }

    public record RateLimit(
            int loginPerIpPerMinute,
            int loginPerAccountPerHour,
            int passwordResetPerAccountPerHour) {
    }

    public record Tokens(Duration emailVerificationTtl, Duration passwordResetTtl) {
    }

    public record Idempotency(Duration ttl) {
    }

    public record Events(String publisher, String topic) {
    }

    public record Outbox(Duration pollInterval, int batchSize) {
    }

    public record Dev(boolean logTokens) {
    }
}
