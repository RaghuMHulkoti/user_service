package com.learning.user_service.core.service.admin.suspend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.service.ServiceRequest;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserServiceRequest implements ServiceRequest {

    private UUID userId;
}
