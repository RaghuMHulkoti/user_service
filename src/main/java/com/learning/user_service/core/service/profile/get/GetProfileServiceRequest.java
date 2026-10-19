package com.learning.user_service.core.service.profile.get;

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
public class GetProfileServiceRequest implements ServiceRequest {

    private UUID userId;
}
