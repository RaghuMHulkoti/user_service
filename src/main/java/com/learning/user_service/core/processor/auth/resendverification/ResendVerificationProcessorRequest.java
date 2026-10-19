package com.learning.user_service.core.processor.auth.resendverification;

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
public class ResendVerificationProcessorRequest implements ProcessorRequest {

    private UUID userId;
}
