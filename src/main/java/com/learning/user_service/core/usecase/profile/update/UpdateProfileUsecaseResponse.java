package com.learning.user_service.core.usecase.profile.update;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileUsecaseResponse implements UsecaseResponse {

    private UserProfile profile;
}
