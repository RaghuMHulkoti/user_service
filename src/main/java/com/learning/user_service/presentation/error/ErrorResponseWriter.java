package com.learning.user_service.presentation.error;

import com.learning.user_service.infrastructure.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;

/** Writes the error envelope directly, for code paths outside Spring MVC (security filters). */
@Component
public class ErrorResponseWriter {

    private final JsonMapper jsonMapper;

    public ErrorResponseWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(HttpServletResponse response, ErrorCode code, String message) throws IOException {
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(code, message, Map.of(), MDC.get(RequestIdFilter.MDC_TRACE_ID));
        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}
