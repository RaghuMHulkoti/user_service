package com.learning.user_service.web.controller;

import com.learning.user_service.core.support.IdempotencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

final class IdempotentResponses {

    static final String KEY_HEADER = "Idempotency-Key";
    static final String REPLAYED_HEADER = "Idempotent-Replayed";

    private IdempotentResponses() {
    }

    static <T> ResponseEntity<T> created(IdempotencyService.Outcome<T> outcome) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.CREATED);
        if (outcome.replayed()) {
            builder.header(REPLAYED_HEADER, "true");
        }
        return builder.body(outcome.body());
    }
}
