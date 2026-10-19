package com.learning.user_service.core.service.auth.login;

import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginServiceResponse implements ServiceResponse {

    private TokenPair tokenPair;
}
