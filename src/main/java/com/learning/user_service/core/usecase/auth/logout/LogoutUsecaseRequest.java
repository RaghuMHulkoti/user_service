package com.learning.user_service.core.usecase.auth.logout;

import com.learning.user_service.core.usecase.UsecaseRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutUsecaseRequest implements UsecaseRequest {

    private UUID userId;
    private String refreshToken;
    private Boolean allSessions;
}
