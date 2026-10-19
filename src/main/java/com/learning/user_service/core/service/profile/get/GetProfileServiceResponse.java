package com.learning.user_service.core.service.profile.get;

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
public class GetProfileServiceResponse implements ServiceResponse {

    private UserProfile profile;
}
