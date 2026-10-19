package com.learning.user_service.core.service.auth.passwordresetrequest;

import com.learning.user_service.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordResetServiceResponse implements ServiceResponse {

    private boolean success;
}
