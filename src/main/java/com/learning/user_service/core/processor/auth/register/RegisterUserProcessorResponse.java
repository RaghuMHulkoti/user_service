package com.learning.user_service.core.processor.auth.register;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import com.learning.user_service.core.domain.RegistrationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserProcessorResponse implements ProcessorResponse {

    private RegistrationResult registrationResult;
    private StatusCode statusCode;
}
