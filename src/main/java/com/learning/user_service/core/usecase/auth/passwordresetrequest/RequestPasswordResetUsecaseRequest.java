package com.learning.user_service.core.usecase.auth.passwordresetrequest;

import com.learning.user_service.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordResetUsecaseRequest implements UsecaseRequest {

    private String email;
}
