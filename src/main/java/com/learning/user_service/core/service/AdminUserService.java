package com.learning.user_service.core.service;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.web.response.PageResponse;
import com.learning.user_service.core.support.ProfileCache;
import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.oracle.user.entity.RoleEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.gateway.oracle.user.repository.RoleRepository;
import com.learning.user_service.gateway.oracle.user.repository.UserRepository;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Operator tools: list users, assign roles, suspend and reactivate accounts. */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final DomainEventRecorder events;
    private final ProfileCache profileCache;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository,
                            RefreshTokenStore refreshTokenStore, DomainEventRecorder events,
                            ProfileCache profileCache) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenStore = refreshTokenStore;
        this.events = events;
        this.profileCache = profileCache;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserProfile> list(UserStatus status, Pageable pageable) {
        Page<UserEntity> page = status == null
                ? userRepository.findAll(pageable)
                : userRepository.findByStatus(status, pageable);
        return PageResponse.from(page, UserProfile::from);
    }

    @Transactional
    public UserProfile setRoles(UUID userId, Set<RoleCode> roles) {
        UserEntity user = findManageable(userId);
        List<RoleEntity> roleEntities = roleRepository.findByCodeIn(roles);
        user.setRoles(new HashSet<>(roleEntities));
        userRepository.flush();
        events.record(userId, UserEventType.USER_UPDATED, Map.of("userId", userId.toString(),
                "changes", List.of("roles"), "roles", roles.stream().map(Enum::name).sorted().toList()));
        profileCache.evict(userId);
        return UserProfile.from(user);
    }

    @Transactional
    public UserProfile suspend(UUID userId) {
        UserEntity user = findManageable(userId);
        if (user.getStatus() == UserStatus.SUSPENDED) {
            return UserProfile.from(user);
        }
        user.setStatus(UserStatus.SUSPENDED);
        return statusChanged(user, true);
    }

    @Transactional
    public UserProfile reactivate(UUID userId) {
        UserEntity user = findManageable(userId);
        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, "Only suspended accounts can be reactivated");
        }
        user.setStatus(user.getEmailVerifiedAt() == null ? UserStatus.PENDING_VERIFICATION : UserStatus.ACTIVE);
        return statusChanged(user, false);
    }

    private UserProfile statusChanged(UserEntity user, boolean revokeSessions) {
        UUID userId = user.getId();
        userRepository.flush();
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
        return UserProfile.from(user);
    }

    private UserEntity findManageable(UUID userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .orElseThrow(() -> ApiException.notFound("User"));
    }
}
