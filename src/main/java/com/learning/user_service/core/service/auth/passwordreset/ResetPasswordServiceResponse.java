package com.learning.user_service.core.service.auth.passwordreset;

import com.learning.user_service.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordServiceResponse implements ServiceResponse {

    private boolean success;
}
