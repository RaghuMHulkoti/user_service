package com.learning.user_service.core.service.auth.verifyemail;

import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.ProfileCache;
import com.learning.user_service.core.support.SecureTokens;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.EmailVerificationGateway;
import com.learning.user_service.gateway.oracle.credential.entity.EmailVerificationEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class VerifyEmailService implements Service<VerifyEmailServiceRequest, VerifyEmailServiceResponse> {

    private final EmailVerificationGateway verificationGateway;
    private final DomainEventRecorder events;
    private final UserAccess userAccess;
    private final ProfileCache profileCache;
    private final Clock clock;

    public VerifyEmailService(EmailVerificationGateway verificationGateway, DomainEventRecorder events,
                              UserAccess userAccess, ProfileCache profileCache, Clock clock) {
        this.verificationGateway = verificationGateway;
        this.events = events;
        this.userAccess = userAccess;
        this.profileCache = profileCache;
        this.clock = clock;
    }

    @Override
    @Transactional
    public VerifyEmailServiceResponse execute(VerifyEmailServiceRequest request) {
        Instant now = clock.instant();
        EmailVerificationEntity verification = verificationGateway
                .findByTokenHash(SecureTokens.sha256Hex(request.getToken()))
                .filter(v -> v.isUsable(now))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN,
                        "Verification link is invalid or has expired"));
        verification.setConsumedAt(now);

        UserEntity user = userAccess.loadUsable(verification.getUserId());
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(now);
        }
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            user.setStatus(UserStatus.ACTIVE);
        }
        events.record(user.getId(), UserEventType.USER_UPDATED, Map.of(
                "userId", user.getId().toString(),
                "changes", List.of("emailVerified", "status"),
                "status", user.getStatus().name()));
        profileCache.evict(user.getId());
        return VerifyEmailServiceResponse.builder().success(true).build();
    }
}
