package com.learning.user_service.application.service;

import com.learning.user_service.application.command.AddPaymentMethodCommand;
import com.learning.user_service.application.query.PaymentMethodView;
import com.learning.user_service.infrastructure.persistence.entity.PaymentMethodTokenEntity;
import com.learning.user_service.infrastructure.persistence.repository.PaymentMethodTokenRepository;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/** Saved payment methods as PSP tokens only. Card numbers never reach this service (PCI SAQ-A). */
@Service
public class PaymentMethodService {

    static final int MAX_METHODS = 10;
    /** 12-19 digits, optionally spaced: looks like a card number, so refuse it. */
    private static final Pattern LOOKS_LIKE_PAN = Pattern.compile("^\\d[\\d -]{10,22}\\d$");

    private final PaymentMethodTokenRepository repository;
    private final UserAccess userAccess;
    private final Clock clock;

    public PaymentMethodService(PaymentMethodTokenRepository repository, UserAccess userAccess, Clock clock) {
        this.repository = repository;
        this.userAccess = userAccess;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodView> list(UUID userId) {
        userAccess.loadUsable(userId);
        return repository.findByUserIdOrderByCreatedAtAsc(userId).stream().map(PaymentMethodView::from).toList();
    }

    @Transactional
    public PaymentMethodView add(UUID userId, AddPaymentMethodCommand cmd) {
        userAccess.loadUsable(userId);
        if (LOOKS_LIKE_PAN.matcher(cmd.providerToken()).matches()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Raw card numbers are not accepted; send the PSP token instead");
        }
        if (cmd.expYear() != null && cmd.expMonth() != null
                && YearMonth.of(cmd.expYear(), cmd.expMonth()).isBefore(YearMonth.now(clock.withZone(ZoneOffset.UTC)))) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, "Payment method has expired");
        }
        if (repository.existsByUserIdAndProviderToken(userId, cmd.providerToken())) {
            throw new ApiException(ErrorCode.CONFLICT, "Payment method is already saved");
        }
        long count = repository.countByUserId(userId);
        if (count >= MAX_METHODS) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Too many saved payment methods (max " + MAX_METHODS + ")");
        }
        boolean makeDefault = Boolean.TRUE.equals(cmd.isDefault()) || count == 0;
        if (makeDefault) {
            repository.clearDefault(userId);
        }
        PaymentMethodTokenEntity method = new PaymentMethodTokenEntity();
        method.setUserId(userId);
        method.setProvider(cmd.provider());
        method.setProviderToken(cmd.providerToken());
        method.setBrand(cmd.brand() == null ? null : cmd.brand().toUpperCase());
        method.setLast4(cmd.last4());
        method.setExpMonth(cmd.expMonth());
        method.setExpYear(cmd.expYear());
        method.setDefaultMethod(makeDefault);
        return PaymentMethodView.from(repository.saveAndFlush(method));
    }

    @Transactional
    public PaymentMethodView makeDefault(UUID userId, UUID methodId) {
        userAccess.loadUsable(userId);
        findOwned(userId, methodId);
        repository.clearDefault(userId);
        PaymentMethodTokenEntity method = findOwned(userId, methodId);
        method.setDefaultMethod(true);
        return PaymentMethodView.from(repository.saveAndFlush(method));
    }

    @Transactional
    public void delete(UUID userId, UUID methodId) {
        userAccess.loadUsable(userId);
        PaymentMethodTokenEntity method = findOwned(userId, methodId);
        boolean wasDefault = method.isDefaultMethod();
        repository.delete(method);
        repository.flush();
        if (wasDefault) {
            repository.findFirstByUserIdOrderByCreatedAtAsc(userId).ifPresent(next -> next.setDefaultMethod(true));
        }
    }

    private PaymentMethodTokenEntity findOwned(UUID userId, UUID methodId) {
        return repository.findByIdAndUserId(methodId, userId)
                .orElseThrow(() -> ApiException.notFound("Payment method"));
    }
}
