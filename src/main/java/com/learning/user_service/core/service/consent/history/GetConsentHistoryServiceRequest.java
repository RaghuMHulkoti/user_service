package com.learning.user_service.core.service.consent.history;

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
public class GetConsentHistoryServiceRequest implements ServiceRequest {

    private UUID userId;
}
