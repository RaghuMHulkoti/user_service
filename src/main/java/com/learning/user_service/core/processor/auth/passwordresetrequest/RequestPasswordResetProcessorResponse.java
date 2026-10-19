package com.learning.user_service.core.processor.auth.passwordresetrequest;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordResetProcessorResponse implements ProcessorResponse {

    private StatusCode statusCode;
}
