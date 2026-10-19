package com.learning.user_service.core.service.profile.update;

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
public class UpdateProfileServiceRequest implements ServiceRequest {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String locale;
    private Long version;
}
