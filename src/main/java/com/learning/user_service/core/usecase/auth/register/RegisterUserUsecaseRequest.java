package com.learning.user_service.core.usecase.auth.register;

import com.learning.user_service.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserUsecaseRequest implements UsecaseRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String locale;
}
