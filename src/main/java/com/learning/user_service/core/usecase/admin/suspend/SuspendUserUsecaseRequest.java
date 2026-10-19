package com.learning.user_service.core.usecase.admin.suspend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.usecase.UsecaseRequest;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserUsecaseRequest implements UsecaseRequest {

    private UUID userId;
}
