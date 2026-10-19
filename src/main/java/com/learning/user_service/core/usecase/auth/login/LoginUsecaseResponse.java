package com.learning.user_service.core.usecase.auth.login;

import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUsecaseResponse implements UsecaseResponse {

    private TokenPair tokenPair;
}
