package com.learning.user_service.core.service.auth.refresh;

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
public class RefreshTokenServiceResponse implements ServiceResponse {

    private TokenPair tokenPair;
}
