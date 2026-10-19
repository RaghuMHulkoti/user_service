package com.learning.user_service.core.service.paymentmethod.add;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.PaymentMethodGateway;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Saved payment methods as PSP tokens only. Card numbers never reach this service (PCI SAQ-A). */
@Component
public class AddPaymentMethodService implements Service<AddPaymentMethodServiceRequest, AddPaymentMethodServiceResponse> {

    static final int MAX_METHODS = 10;
    /** 12-19 digits, optionally spaced: looks like a card number, so refuse it. */
    private static final Pattern LOOKS_LIKE_PAN = Pattern.compile("^\\d[\\d -]{10,22}\\d$");

    private final PaymentMethodGateway paymentMethodGateway;
    private final UserAccess userAccess;
    private final Clock clock;

    public AddPaymentMethodService(PaymentMethodGateway paymentMethodGateway, UserAccess userAccess, Clock clock) {
        this.paymentMethodGateway = paymentMethodGateway;
        this.userAccess = userAccess;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AddPaymentMethodServiceResponse execute(AddPaymentMethodServiceRequest request) {
        UUID userId = request.getUserId();
        userAccess.loadUsable(userId);
        if (LOOKS_LIKE_PAN.matcher(request.getProviderToken()).matches()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Raw card numbers are not accepted; send the PSP token instead");
        }
        if (request.getExpYear() != null && request.getExpMonth() != null
                && YearMonth.of(request.getExpYear(), request.getExpMonth()).isBefore(YearMonth.now(clock.withZone(ZoneOffset.UTC)))) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, "Payment method has expired");
        }
        if (paymentMethodGateway.existsByUserIdAndProviderToken(userId, request.getProviderToken())) {
            throw new ApiException(ErrorCode.CONFLICT, "Payment method is already saved");
        }
        long count = paymentMethodGateway.countByUserId(userId);
        if (count >= MAX_METHODS) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Too many saved payment methods (max " + MAX_METHODS + ")");
        }
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault()) || count == 0;
        if (makeDefault) {
            paymentMethodGateway.clearDefault(userId);
        }
        PaymentMethodTokenEntity method = new PaymentMethodTokenEntity();
        method.setUserId(userId);
        method.setProvider(request.getProvider());
        method.setProviderToken(request.getProviderToken());
        method.setBrand(request.getBrand() == null ? null : request.getBrand().toUpperCase());
        method.setLast4(request.getLast4());
        method.setExpMonth(request.getExpMonth());
        method.setExpYear(request.getExpYear());
        method.setDefaultMethod(makeDefault);
        return AddPaymentMethodServiceResponse.builder()
                .method(paymentMethodGateway.saveAndFlush(method))
                .build();
    }
}
