package com.learning.user_service.core.service.auth.resendverification;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.VerificationTokenIssuer;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.EmailVerificationGateway;
import com.learning.user_service.gateway.RateLimiter;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

@Component
public class ResendVerificationService
        implements Service<ResendVerificationServiceRequest, ResendVerificationServiceResponse> {

    private final EmailVerificationGateway verificationGateway;
    private final RateLimiter rateLimiter;
    private final UserAccess userAccess;
    private final VerificationTokenIssuer verificationTokenIssuer;
    private final Clock clock;

    public ResendVerificationService(EmailVerificationGateway verificationGateway, RateLimiter rateLimiter,
                                     UserAccess userAccess, VerificationTokenIssuer verificationTokenIssuer,
                                     Clock clock) {
        this.verificationGateway = verificationGateway;
        this.rateLimiter = rateLimiter;
        this.userAccess = userAccess;
        this.verificationTokenIssuer = verificationTokenIssuer;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ResendVerificationServiceResponse execute(ResendVerificationServiceRequest request) {
        UUID userId = request.getUserId();
        UserEntity user = userAccess.loadUsable(userId);
        if (user.getEmailVerifiedAt() != null) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, "Email is already verified");
        }
        if (!rateLimiter.tryAcquire("verify-resend:" + userId, 3, Duration.ofHours(1))) {
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many verification emails; try again later");
        }
        verificationGateway.consumeAllOpen(userId, clock.instant());
        verificationTokenIssuer.issue(user);
        return ResendVerificationServiceResponse.builder().success(true).build();
    }
}
