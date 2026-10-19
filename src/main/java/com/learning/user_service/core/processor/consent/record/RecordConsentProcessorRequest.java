package com.learning.user_service.core.processor.consent.record;

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
public class RecordConsentProcessorRequest implements ProcessorRequest {

    private UUID userId;
    private String kind;
    private Boolean granted;
    private String clientIp;
}
