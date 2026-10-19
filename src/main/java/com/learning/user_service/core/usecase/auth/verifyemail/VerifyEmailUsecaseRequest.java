package com.learning.user_service.core.usecase.auth.verifyemail;

import com.learning.user_service.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailUsecaseRequest implements UsecaseRequest {

    private String token;
}
