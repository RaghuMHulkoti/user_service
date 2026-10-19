package com.learning.user_service.core.usecase.auth.passwordreset;

import com.learning.user_service.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordUsecaseRequest implements UsecaseRequest {

    private String token;
    private String newPassword;
}
