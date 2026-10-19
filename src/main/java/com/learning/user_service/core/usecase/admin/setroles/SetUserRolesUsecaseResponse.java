package com.learning.user_service.core.usecase.admin.setroles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.usecase.UsecaseResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetUserRolesUsecaseResponse implements UsecaseResponse {

    private UserProfile profile;
}
