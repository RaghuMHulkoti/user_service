package com.learning.user_service.core.usecase.auth.resendverification;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationUsecaseResponse implements UsecaseResponse {

    private boolean success;
}
