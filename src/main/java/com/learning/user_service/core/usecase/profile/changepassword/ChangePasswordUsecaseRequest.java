package com.learning.user_service.core.usecase.profile.changepassword;

import com.learning.user_service.core.usecase.UsecaseRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordUsecaseRequest implements UsecaseRequest {

    private UUID userId;
    private String currentPassword;
    private String newPassword;
}
