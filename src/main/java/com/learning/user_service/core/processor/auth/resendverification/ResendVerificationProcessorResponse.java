package com.learning.user_service.core.processor.auth.resendverification;

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
public class ResendVerificationProcessorResponse implements ProcessorResponse {

    private boolean success;
    private StatusCode statusCode;
}
