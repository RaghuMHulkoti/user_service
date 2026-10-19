package com.learning.user_service.core.usecase.auth.login;

import com.learning.user_service.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUsecaseRequest implements UsecaseRequest {

    private String email;
    private String password;
    private String clientIp;
}
