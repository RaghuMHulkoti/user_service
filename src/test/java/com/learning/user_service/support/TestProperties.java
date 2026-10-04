package com.learning.user_service.support;

import com.learning.user_service.infrastructure.config.AppProperties;

import java.time.Duration;

public final class TestProperties {

    private TestProperties() {
    }

    public static AppProperties defaults() {
        return new AppProperties(
                new AppProperties.Security("http://user-service", "bazaar", Duration.ofMinutes(15),
                        Duration.ofDays(30), 4, "", "", "test-key", "internal-token", 5, Duration.ofMinutes(15)),
                new AppProperties.RateLimit(5, 10, 3),
                new AppProperties.Tokens(Duration.ofHours(24), Duration.ofMinutes(30)),
                new AppProperties.Idempotency(Duration.ofHours(24)),
                new AppProperties.Events("log", "domain.user.events"),
                new AppProperties.Outbox(Duration.ofSeconds(1), 100),
                new AppProperties.Dev(false));
    }
}
