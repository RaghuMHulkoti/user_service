package com.learning.user_service.core.support;

import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.domain.policy.PasswordPolicy;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.RefreshTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Shared by the reset-password and change-password operations. */
@Component
public class PasswordChangeSupport {

    private final DomainEventRecorder events;
    private final RefreshTokenStore refreshTokenStore;

    public PasswordChangeSupport(DomainEventRecorder events, RefreshTokenStore refreshTokenStore) {
        this.events = events;
        this.refreshTokenStore = refreshTokenStore;
    }

    /** Signs the user out everywhere once the new password is committed. */
    public void passwordChanged(UUID userId, String reason) {
        events.record(userId, UserEventType.PASSWORD_CHANGED, Map.of("userId", userId.toString(), "reason", reason));
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                refreshTokenStore.revokeAllForUser(userId);
            }
        });
    }

    public static void requireStrong(String password, String field) {
        List<String> problems = PasswordPolicy.violations(password);
        if (!problems.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Request validation failed",
                    Map.of("fields", Map.of(field, String.join("; ", problems))));
        }
    }
}
