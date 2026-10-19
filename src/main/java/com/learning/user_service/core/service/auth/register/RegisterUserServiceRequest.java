package com.learning.user_service.core.service.auth.register;

import com.learning.user_service.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserServiceRequest implements ServiceRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String locale;
}
