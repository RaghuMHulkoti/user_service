package com.learning.user_service.core.service.admin.suspend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.service.ServiceResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserServiceResponse implements ServiceResponse {

    private UserProfile profile;
}
