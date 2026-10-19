package com.learning.user_service.core.service.consent.record;

import com.learning.user_service.core.service.ServiceRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordConsentServiceRequest implements ServiceRequest {

    private UUID userId;
    private String kind;
    private Boolean granted;
    private String clientIp;
}
