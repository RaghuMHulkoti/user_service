package com.learning.user_service.core.usecase.profile.update;

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
public class UpdateProfileUsecaseRequest implements UsecaseRequest {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String locale;
    private Long version;
}
