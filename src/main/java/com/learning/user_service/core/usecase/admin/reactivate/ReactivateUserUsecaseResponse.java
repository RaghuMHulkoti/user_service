package com.learning.user_service.core.usecase.admin.reactivate;

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
public class ReactivateUserUsecaseResponse implements UsecaseResponse {

    private UserProfile profile;
}
