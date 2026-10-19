package com.learning.user_service.core.service.auth.passwordresetrequest;

import com.learning.user_service.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordResetServiceRequest implements ServiceRequest {

    private String email;
}
