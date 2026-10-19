package com.learning.user_service.core.support;

import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Shared by the operator status operations (suspend, reactivate) and role assignment. */
@Component
public class AdminUserSupport {

    private final UserGateway userGateway;
    private final RefreshTokenStore refreshTokenStore;
    private final DomainEventRecorder events;
    private final ProfileCache profileCache;

    public AdminUserSupport(UserGateway userGateway, RefreshTokenStore refreshTokenStore,
                            DomainEventRecorder events, ProfileCache profileCache) {
        this.userGateway = userGateway;
        this.refreshTokenStore = refreshTokenStore;
        this.events = events;
        this.profileCache = profileCache;
    }

    public void statusChanged(UserEntity user, boolean revokeSessions) {
        UUID userId = user.getId();
        userGateway.flush();
        events.record(userId, UserEventType.USER_UPDATED, Map.of("userId", userId.toString(),
                "changes", List.of("status"), "status", user.getStatus().name()));
        profileCache.evict(userId);
        if (revokeSessions) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    refreshTokenStore.revokeAllForUser(userId);
                }
            });
        }
    }

    public UserEntity findManageable(UUID userId) {
        return userGateway.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .orElseThrow(() -> ApiException.notFound("User"));
    }
}
