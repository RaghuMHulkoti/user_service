package com.learning.user_service.core.processor.auth.logout;

import com.learning.user_service.core.processor.ProcessorRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutProcessorRequest implements ProcessorRequest {

    private UUID userId;
    private String refreshToken;
    private Boolean allSessions;
}
