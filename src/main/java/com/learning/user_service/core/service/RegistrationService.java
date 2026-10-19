package com.learning.user_service.core.service;

import com.learning.user_service.web.model.RegisterUserWebRequest;
import com.learning.user_service.web.model.VerifyEmailWebRequest;
import com.learning.user_service.core.domain.RegistrationResult;
import com.learning.user_service.core.support.DevTokenLogger;
import com.learning.user_service.core.support.EmailNormalizer;
import com.learning.user_service.core.support.ProfileCache;
import com.learning.user_service.core.support.SecureTokens;
import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.domain.policy.PasswordPolicy;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.RateLimiter;
import com.learning.user_service.config.AppProperties;
import com.learning.user_service.gateway.oracle.credential.entity.EmailVerificationEntity;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.gateway.oracle.credential.repository.EmailVerificationRepository;
import com.learning.user_service.gateway.oracle.credential.repository.PasswordCredentialRepository;
import com.learning.user_service.gateway.oracle.user.repository.RoleRepository;
import com.learning.user_service.gateway.oracle.user.repository.UserRepository;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Account creation and email verification (TDD §8.2, §12.1). */
@Service
public class RegistrationService {

    private static final String DEFAULT_LOCALE = "en-IN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventRecorder events;
    private final RateLimiter rateLimiter;
    private final DevTokenLogger devTokenLogger;
    private final UserAccess userAccess;
    private final ProfileCache profileCache;
    private final Duration verificationTtl;
    private final Clock clock;

    public RegistrationService(UserRepository userRepository, RoleRepository roleRepository,
                               PasswordCredentialRepository credentialRepository,
                               EmailVerificationRepository verificationRepository, PasswordEncoder passwordEncoder,
                               DomainEventRecorder events, RateLimiter rateLimiter, DevTokenLogger devTokenLogger,
                               UserAccess userAccess, ProfileCache profileCache, AppProperties properties,
                               Clock clock) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.credentialRepository = credentialRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
        this.rateLimiter = rateLimiter;
        this.devTokenLogger = devTokenLogger;
        this.userAccess = userAccess;
        this.profileCache = profileCache;
        this.verificationTtl = properties.tokens().emailVerificationTtl();
        this.clock = clock;
    }

    /** One transaction: user + credentials + role + verification token + outbox events. */
    @Transactional
    public RegistrationResult register(RegisterUserWebRequest cmd) {
        List<String> passwordProblems = PasswordPolicy.violations(cmd.password());
        if (!passwordProblems.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Request validation failed",
                    Map.of("fields", Map.of("password", String.join("; ", passwordProblems))));
        }
        String email = EmailNormalizer.normalize(cmd.email());
        if (userRepository.existsActiveByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_REGISTERED, "An account with this email already exists");
        }
        if (cmd.phone() != null && userRepository.existsActiveByPhone(cmd.phone())) {
            throw new ApiException(ErrorCode.PHONE_ALREADY_REGISTERED, "An account with this phone already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPhone(cmd.phone());
        user.setFirstName(cmd.firstName().trim());
        user.setLastName(cmd.lastName() == null ? null : cmd.lastName().trim());
        user.setLocale(cmd.locale() == null ? DEFAULT_LOCALE : cmd.locale());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.getRoles().add(roleRepository.findByCode(RoleCode.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role missing")));
        user = userRepository.saveAndFlush(user);

        credentialRepository.save(new PasswordCredentialEntity(user.getId(), passwordEncoder.encode(cmd.password())));

        Map<String, Object> registered = new HashMap<>();
        registered.put("userId", user.getId().toString());
        registered.put("email", user.getEmail());
        registered.put("firstName", user.getFirstName());
        registered.put("lastName", user.getLastName());
        registered.put("phone", user.getPhone());
        registered.put("locale", user.getLocale());
        registered.put("roles", List.of(RoleCode.CUSTOMER.name()));
        events.record(user.getId(), UserEventType.USER_REGISTERED, registered);

        issueVerificationToken(user);
        return new RegistrationResult(user.getId(), user.getEmail(), user.getStatus(), true);
    }

    @Transactional
    public void verifyEmail(VerifyEmailWebRequest cmd) {
        Instant now = clock.instant();
        EmailVerificationEntity verification = verificationRepository
                .findByTokenHash(SecureTokens.sha256Hex(cmd.token()))
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
    }

    @Transactional
    public void resendVerification(UUID userId) {
        UserEntity user = userAccess.loadUsable(userId);
        if (user.getEmailVerifiedAt() != null) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, "Email is already verified");
        }
        if (!rateLimiter.tryAcquire("verify-resend:" + userId, 3, Duration.ofHours(1))) {
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many verification emails; try again later");
        }
        verificationRepository.consumeAllOpen(userId, clock.instant());
        issueVerificationToken(user);
    }

    private void issueVerificationToken(UserEntity user) {
        String rawToken = SecureTokens.newToken();
        Instant expiresAt = clock.instant().plus(verificationTtl);
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setUserId(user.getId());
        verification.setTokenHash(SecureTokens.sha256Hex(rawToken));
        verification.setExpiresAt(expiresAt);
        verificationRepository.save(verification);

        // The notification service needs the raw token to build the link. Email failures are
        // outboxed and never fail registration (TDD §8.2).
        events.record(user.getId(), UserEventType.EMAIL_VERIFICATION_REQUESTED, Map.of(
                "userId", user.getId().toString(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "locale", user.getLocale(),
                "verificationToken", rawToken,
                "expiresAt", expiresAt.toString()));
        devTokenLogger.log("Email verification", user.getId(), rawToken);
    }
}
