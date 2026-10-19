package com.learning.user_service.core.support;

import com.learning.user_service.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Dev only: prints raw one-time tokens so flows can be tested without the notification service. */
@Component
public class DevTokenLogger {

    private static final Logger log = LoggerFactory.getLogger(DevTokenLogger.class);

    private final boolean enabled;

    public DevTokenLogger(AppProperties properties) {
        this.enabled = properties.dev() != null && properties.dev().logTokens();
    }

    public void log(String purpose, UUID userId, String rawToken) {
        if (enabled) {
            log.info("[DEV] {} token for user {}: {}", purpose, userId, rawToken);
        }
    }
}
