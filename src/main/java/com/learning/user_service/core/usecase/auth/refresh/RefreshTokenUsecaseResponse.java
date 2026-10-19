package com.learning.user_service.core.usecase.auth.refresh;

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
public class RefreshTokenUsecaseResponse implements UsecaseResponse {

    private TokenPair tokenPair;
}
