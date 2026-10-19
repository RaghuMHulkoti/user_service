package com.learning.user_service.core.service.auth.passwordresetrequest;

import com.learning.user_service.config.AppProperties;
import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.DevTokenLogger;
import com.learning.user_service.core.support.EmailNormalizer;
import com.learning.user_service.core.support.SecureTokens;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.PasswordResetTokenGateway;
import com.learning.user_service.gateway.RateLimiter;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordResetTokenEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Component
public class RequestPasswordResetService
        implements Service<RequestPasswordResetServiceRequest, RequestPasswordResetServiceResponse> {

    private final UserGateway userGateway;
    private final PasswordResetTokenGateway resetTokenGateway;
    private final DomainEventRecorder events;
    private final RateLimiter rateLimiter;
    private final DevTokenLogger devTokenLogger;
    private final AppProperties properties;
    private final Clock clock;

    public RequestPasswordResetService(UserGateway userGateway, PasswordResetTokenGateway resetTokenGateway,
                                       DomainEventRecorder events, RateLimiter rateLimiter,
                                       DevTokenLogger devTokenLogger, AppProperties properties, Clock clock) {
        this.userGateway = userGateway;
        this.resetTokenGateway = resetTokenGateway;
        this.events = events;
        this.rateLimiter = rateLimiter;
        this.devTokenLogger = devTokenLogger;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Always succeeds from the caller's view, whether or not the email exists,
     * so the endpoint cannot be used to discover accounts.
     */
    @Override
    @Transactional
    public RequestPasswordResetServiceResponse execute(RequestPasswordResetServiceRequest request) {
        String email = EmailNormalizer.normalize(request.getEmail());
        Optional<UserEntity> found = userGateway.findActiveByEmail(email).filter(u -> u.getStatus().canLogIn());
        if (found.isEmpty()) {
            return success();
        }
        if (!rateLimiter.tryAcquire("password-reset:" + email,
                properties.rateLimit().passwordResetPerAccountPerHour(), Duration.ofHours(1))) {
            return success(); // silently drop: same response as success
        }
        UserEntity user = found.get();
        Instant now = clock.instant();
        resetTokenGateway.consumeAllOpen(user.getId(), now);

        String rawToken = SecureTokens.newToken();
        Instant expiresAt = now.plus(properties.tokens().passwordResetTtl());
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUserId(user.getId());
        token.setTokenHash(SecureTokens.sha256Hex(rawToken));
        token.setExpiresAt(expiresAt);
        resetTokenGateway.save(token);

        events.record(user.getId(), UserEventType.PASSWORD_RESET_REQUESTED, Map.of(
                "userId", user.getId().toString(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "locale", user.getLocale(),
                "resetToken", rawToken,
                "expiresAt", expiresAt.toString()));
        devTokenLogger.log("Password reset", user.getId(), rawToken);
        return success();
    }

    private static RequestPasswordResetServiceResponse success() {
        return RequestPasswordResetServiceResponse.builder().success(true).build();
    }
}
