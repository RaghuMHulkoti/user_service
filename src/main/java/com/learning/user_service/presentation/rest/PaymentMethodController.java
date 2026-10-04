package com.learning.user_service.presentation.rest;

import com.learning.user_service.application.command.AddPaymentMethodCommand;
import com.learning.user_service.application.query.PaymentMethodView;
import com.learning.user_service.application.service.PaymentMethodService;
import com.learning.user_service.application.support.IdempotencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/payment-methods")
@Tag(name = "Payment methods", description = "PSP tokens only; never card numbers")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final IdempotencyService idempotencyService;

    public PaymentMethodController(PaymentMethodService paymentMethodService, IdempotencyService idempotencyService) {
        this.paymentMethodService = paymentMethodService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "List saved payment methods")
    @GetMapping
    public List<PaymentMethodView> list(@AuthenticationPrincipal Jwt jwt) {
        return paymentMethodService.list(CurrentUser.id(jwt));
    }

    @Operation(summary = "Save a payment method from a PSP token")
    @PostMapping
    public ResponseEntity<PaymentMethodView> add(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = IdempotentResponses.KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody AddPaymentMethodCommand cmd) {
        UUID userId = CurrentUser.id(jwt);
        return IdempotentResponses.created(idempotencyService.execute("payment-methods:" + userId, idempotencyKey,
                cmd, PaymentMethodView.class, () -> paymentMethodService.add(userId, cmd)));
    }

    @Operation(summary = "Make a payment method the default")
    @PostMapping("/{id}/default")
    public PaymentMethodView makeDefault(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return paymentMethodService.makeDefault(CurrentUser.id(jwt), id);
    }

    @Operation(summary = "Remove a payment method")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        paymentMethodService.delete(CurrentUser.id(jwt), id);
    }
}
