package com.learning.user_service.core.usecase.profile.delete;

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
public class DeleteAccountUsecaseRequest implements UsecaseRequest {

    private UUID userId;
}
