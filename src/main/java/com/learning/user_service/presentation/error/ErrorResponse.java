package com.learning.user_service.presentation.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Error envelope from TDD §11.1: {@code { "error": { code, message, details, traceId } }}. */
public record ErrorResponse(Body error) {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record Body(String code, String message, Map<String, Object> details, String traceId) {
    }

    public static ErrorResponse of(ErrorCode code, String message, Map<String, Object> details, String traceId) {
        return new ErrorResponse(new Body(code.name(), message, details, traceId));
    }
}
