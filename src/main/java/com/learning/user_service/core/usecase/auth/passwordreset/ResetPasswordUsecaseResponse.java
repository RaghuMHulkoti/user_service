package com.learning.user_service.core.usecase.auth.passwordreset;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordUsecaseResponse implements UsecaseResponse {

    private boolean success;
}
