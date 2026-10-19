package com.learning.user_service.core.service.auth.passwordreset;

import com.learning.user_service.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordServiceRequest implements ServiceRequest {

    private String token;
    private String newPassword;
}
