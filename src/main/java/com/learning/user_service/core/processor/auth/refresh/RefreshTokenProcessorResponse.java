package com.learning.user_service.core.processor.auth.refresh;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenProcessorResponse implements ProcessorResponse {

    private TokenPair tokenPair;
    private StatusCode statusCode;
}
