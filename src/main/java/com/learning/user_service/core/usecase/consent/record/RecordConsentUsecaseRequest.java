package com.learning.user_service.core.usecase.consent.record;

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
public class RecordConsentUsecaseRequest implements UsecaseRequest {

    private UUID userId;
    private String kind;
    private Boolean granted;
    private String clientIp;
}
