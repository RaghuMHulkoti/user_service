package com.learning.user_service.core.processor.profile.changepassword;

import com.learning.user_service.core.processor.ProcessorRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordProcessorRequest implements ProcessorRequest {

    private UUID userId;
    private String currentPassword;
    private String newPassword;
}
