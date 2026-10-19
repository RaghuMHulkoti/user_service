package com.learning.user_service.web.controller;

import com.learning.user_service.web.model.AddPaymentMethodWebRequest;
import com.learning.user_service.web.response.PaymentMethodResponse;
import com.learning.user_service.core.support.IdempotencyService;
import com.learning.user_service.core.processor.paymentmethod.list.ListPaymentMethodsProcessor;
import com.learning.user_service.core.processor.paymentmethod.add.AddPaymentMethodProcessor;
import com.learning.user_service.core.processor.paymentmethod.makedefault.MakePaymentMethodDefaultProcessor;
import com.learning.user_service.core.processor.paymentmethod.delete.DeletePaymentMethodProcessor;
import com.learning.user_service.web.mapper.PaymentMethodMapper;
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

    private final ListPaymentMethodsProcessor listPaymentMethodsProcessor;
    private final AddPaymentMethodProcessor addPaymentMethodProcessor;
    private final MakePaymentMethodDefaultProcessor makePaymentMethodDefaultProcessor;
    private final DeletePaymentMethodProcessor deletePaymentMethodProcessor;
    private final IdempotencyService idempotencyService;

    public PaymentMethodController(ListPaymentMethodsProcessor listPaymentMethodsProcessor,
                                   AddPaymentMethodProcessor addPaymentMethodProcessor,
                                   MakePaymentMethodDefaultProcessor makePaymentMethodDefaultProcessor,
                                   DeletePaymentMethodProcessor deletePaymentMethodProcessor,
                                   IdempotencyService idempotencyService) {
        this.listPaymentMethodsProcessor = listPaymentMethodsProcessor;
        this.addPaymentMethodProcessor = addPaymentMethodProcessor;
        this.makePaymentMethodDefaultProcessor = makePaymentMethodDefaultProcessor;
        this.deletePaymentMethodProcessor = deletePaymentMethodProcessor;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "List saved payment methods")
    @GetMapping
    public List<PaymentMethodResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return PaymentMethodMapper.mapListPaymentMethodsResponse(listPaymentMethodsProcessor.process(
                PaymentMethodMapper.toListPaymentMethodsRequest(CurrentUser.id(jwt))));
    }

    @Operation(summary = "Save a payment method from a PSP token")
    @PostMapping
    public ResponseEntity<PaymentMethodResponse> add(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = IdempotentResponses.KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody AddPaymentMethodWebRequest cmd) {
        UUID userId = CurrentUser.id(jwt);
        return IdempotentResponses.created(idempotencyService.execute("payment-methods:" + userId, idempotencyKey,
                cmd, PaymentMethodResponse.class, () -> PaymentMethodMapper.mapAddPaymentMethodResponse(addPaymentMethodProcessor.process(
                        PaymentMethodMapper.toAddPaymentMethodRequest(userId, cmd)))));
    }

    @Operation(summary = "Make a payment method the default")
    @PostMapping("/{id}/default")
    public PaymentMethodResponse makeDefault(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return PaymentMethodMapper.mapMakePaymentMethodDefaultResponse(makePaymentMethodDefaultProcessor.process(
                PaymentMethodMapper.toMakePaymentMethodDefaultRequest(CurrentUser.id(jwt), id)));
    }

    @Operation(summary = "Remove a payment method")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        deletePaymentMethodProcessor.process(
                PaymentMethodMapper.toDeletePaymentMethodRequest(CurrentUser.id(jwt), id));
    }
}
