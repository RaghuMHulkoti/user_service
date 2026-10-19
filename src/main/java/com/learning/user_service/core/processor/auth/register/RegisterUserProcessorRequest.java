package com.learning.user_service.core.processor.auth.register;

import com.learning.user_service.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserProcessorRequest implements ProcessorRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String locale;
}
