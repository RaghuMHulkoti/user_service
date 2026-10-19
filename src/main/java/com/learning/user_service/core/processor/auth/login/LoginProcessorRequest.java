package com.learning.user_service.core.processor.auth.login;

import com.learning.user_service.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginProcessorRequest implements ProcessorRequest {

    private String email;
    private String password;
    private String clientIp;
}
