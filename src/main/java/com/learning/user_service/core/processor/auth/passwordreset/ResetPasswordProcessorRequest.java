package com.learning.user_service.core.processor.auth.passwordreset;

import com.learning.user_service.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordProcessorRequest implements ProcessorRequest {

    private String token;
    private String newPassword;
}
