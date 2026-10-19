package com.learning.user_service.core.processor.auth.verifyemail;

import com.learning.user_service.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailProcessorRequest implements ProcessorRequest {

    private String token;
}
