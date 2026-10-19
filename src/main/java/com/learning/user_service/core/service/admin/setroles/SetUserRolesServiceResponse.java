package com.learning.user_service.core.service.admin.setroles;

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
public class SetUserRolesServiceResponse implements ServiceResponse {

    private UserProfile profile;
}
