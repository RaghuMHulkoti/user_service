package com.learning.user_service.core.service.profile.delete;

import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.ProfileCache;
import com.learning.user_service.gateway.AddressGateway;
import com.learning.user_service.gateway.ConsentGateway;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.PasswordCredentialGateway;
import com.learning.user_service.gateway.PaymentMethodGateway;
import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class DeleteAccountService implements Service<DeleteAccountServiceRequest, DeleteAccountServiceResponse> {

    private final AddressGateway addressGateway;
    private final PaymentMethodGateway paymentMethodGateway;
    private final PasswordCredentialGateway credentialGateway;
    private final ConsentGateway consentGateway;
    private final RefreshTokenStore refreshTokenStore;
    private final DomainEventRecorder events;
    private final UserAccess userAccess;
    private final ProfileCache profileCache;
    private final Clock clock;

    public DeleteAccountService(AddressGateway addressGateway, PaymentMethodGateway paymentMethodGateway,
                                PasswordCredentialGateway credentialGateway, ConsentGateway consentGateway,
                                RefreshTokenStore refreshTokenStore, DomainEventRecorder events,
                                UserAccess userAccess, ProfileCache profileCache, Clock clock) {
        this.addressGateway = addressGateway;
        this.paymentMethodGateway = paymentMethodGateway;
        this.credentialGateway = credentialGateway;
        this.consentGateway = consentGateway;
        this.refreshTokenStore = refreshTokenStore;
        this.events = events;
        this.userAccess = userAccess;
        this.profileCache = profileCache;
        this.clock = clock;
    }

    /**
     * Right to erasure (TDD §15.7): anonymise personal data, remove addresses and payment
     * tokens, revoke sessions, and emit a USER_DELETED tombstone so other services purge too.
     */
    @Override
    @Transactional
    public DeleteAccountServiceResponse execute(DeleteAccountServiceRequest request) {
        UUID userId = request.getUserId();
        UserEntity user = userAccess.loadUsable(userId);
        Instant now = clock.instant();
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(now);
        user.setEmail("deleted+" + userId + "@deleted.invalid");
        user.setPhone(null);
        user.setFirstName("Deleted");
        user.setLastName(null);
        user.getRoles().clear();
        addressGateway.deleteAll(addressGateway.findByUserId(userId));
        paymentMethodGateway.deleteByUserId(userId);
        credentialGateway.deleteById(userId);
        consentGateway.clearIpByUserId(userId);

        events.record(userId, UserEventType.USER_DELETED, Map.of("userId", userId.toString(),
                "deletedAt", now.toString()));
        profileCache.evict(userId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                refreshTokenStore.revokeAllForUser(userId);
            }
        });
        return DeleteAccountServiceResponse.builder().success(true).build();
    }
}
