package com.learning.user_service.core.service.auth.login;

import com.learning.user_service.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginServiceRequest implements ServiceRequest {

    private String email;
    private String password;
    private String clientIp;
}
