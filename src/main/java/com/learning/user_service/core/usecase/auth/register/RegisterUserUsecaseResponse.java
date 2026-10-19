package com.learning.user_service.core.usecase.auth.register;

import com.learning.user_service.core.usecase.UsecaseResponse;
import com.learning.user_service.core.domain.RegistrationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserUsecaseResponse implements UsecaseResponse {

    private RegistrationResult registrationResult;
}
