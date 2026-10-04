package com.learning.user_service.application.service;

import com.learning.user_service.application.command.UpdateProfileCommand;
import com.learning.user_service.application.support.ProfileCache;
import com.learning.user_service.domain.event.UserEventType;
import com.learning.user_service.domain.model.UserStatus;
import com.learning.user_service.domain.port.DomainEventRecorder;
import com.learning.user_service.domain.port.RefreshTokenStore;
import com.learning.user_service.infrastructure.persistence.entity.UserEntity;
import com.learning.user_service.infrastructure.persistence.repository.AddressRepository;
import com.learning.user_service.infrastructure.persistence.repository.PasswordCredentialRepository;
import com.learning.user_service.infrastructure.persistence.repository.PaymentMethodTokenRepository;
import com.learning.user_service.infrastructure.persistence.repository.UserRepository;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Profile changes and account deletion. */
@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodTokenRepository paymentMethodRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final DomainEventRecorder events;
    private final UserAccess userAccess;
    private final ProfileCache profileCache;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public UserProfileService(UserRepository userRepository, AddressRepository addressRepository,
                              PaymentMethodTokenRepository paymentMethodRepository,
                              PasswordCredentialRepository credentialRepository, RefreshTokenStore refreshTokenStore,
                              DomainEventRecorder events, UserAccess userAccess, ProfileCache profileCache,
                              JdbcTemplate jdbc, Clock clock) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.credentialRepository = credentialRepository;
        this.refreshTokenStore = refreshTokenStore;
        this.events = events;
        this.userAccess = userAccess;
        this.profileCache = profileCache;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional
    public UserProfile update(UUID userId, UpdateProfileCommand cmd) {
        UserEntity user = userAccess.loadUsable(userId);
        if (cmd.version() != null && cmd.version() != user.getVersion()) {
            throw new ObjectOptimisticLockingFailureException(UserEntity.class, userId);
        }
        List<String> changes = new ArrayList<>();
        if (cmd.firstName() != null && !cmd.firstName().trim().equals(user.getFirstName())) {
            if (cmd.firstName().isBlank()) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "firstName must not be blank");
            }
            user.setFirstName(cmd.firstName().trim());
            changes.add("firstName");
        }
        if (cmd.lastName() != null && !Objects.equals(cmd.lastName().trim(), user.getLastName())) {
            user.setLastName(cmd.lastName().isBlank() ? null : cmd.lastName().trim());
            changes.add("lastName");
        }
        if (cmd.phone() != null && !cmd.phone().equals(user.getPhone())) {
            if (userRepository.existsActiveByPhone(cmd.phone())) {
                throw new ApiException(ErrorCode.PHONE_ALREADY_REGISTERED, "An account with this phone already exists");
            }
            user.setPhone(cmd.phone());
            user.setPhoneVerifiedAt(null);
            changes.add("phone");
        }
        if (cmd.locale() != null && !cmd.locale().equals(user.getLocale())) {
            user.setLocale(cmd.locale());
            changes.add("locale");
        }
        if (!changes.isEmpty()) {
            userRepository.flush();
            events.record(userId, UserEventType.USER_UPDATED, Map.of(
                    "userId", userId.toString(),
                    "changes", changes,
                    "firstName", user.getFirstName(),
                    "lastName", Objects.toString(user.getLastName(), ""),
                    "locale", user.getLocale()));
            profileCache.evict(userId);
        }
        return UserProfile.from(user);
    }

    /**
     * Right to erasure (TDD §15.7): anonymise personal data, remove addresses and payment
     * tokens, revoke sessions, and emit a USER_DELETED tombstone so other services purge too.
     */
    @Transactional
    public void deleteAccount(UUID userId) {
        UserEntity user = userAccess.loadUsable(userId);
        Instant now = clock.instant();
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(now);
        user.setEmail("deleted+" + userId + "@deleted.invalid");
        user.setPhone(null);
        user.setFirstName("Deleted");
        user.setLastName(null);
        user.getRoles().clear();
        addressRepository.deleteAll(addressRepository.findByUserIdOrderByCreatedAtAsc(userId));
        paymentMethodRepository.deleteByUserId(userId);
        credentialRepository.deleteById(userId);
        jdbc.update("update consent_log set ip = null where user_id = ?", userId);

        events.record(userId, UserEventType.USER_DELETED, Map.of("userId", userId.toString(),
                "deletedAt", now.toString()));
        profileCache.evict(userId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                refreshTokenStore.revokeAllForUser(userId);
            }
        });
    }
}
