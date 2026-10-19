package com.learning.user_service.core.usecase.admin.suspend;

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
public class SuspendUserUsecaseResponse implements UsecaseResponse {

    private UserProfile profile;
}
