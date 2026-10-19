package com.learning.user_service.core.service.profile.update;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileServiceResponse implements ServiceResponse {

    private UserProfile profile;
}
