package com.learning.user_service.core.service.auth.register;

import com.learning.user_service.core.service.ServiceResponse;
import com.learning.user_service.core.domain.RegistrationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserServiceResponse implements ServiceResponse {

    private RegistrationResult registrationResult;
}
