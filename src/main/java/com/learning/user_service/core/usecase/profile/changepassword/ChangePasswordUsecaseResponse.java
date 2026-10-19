package com.learning.user_service.core.usecase.profile.changepassword;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordUsecaseResponse implements UsecaseResponse {

    private boolean success;
}
