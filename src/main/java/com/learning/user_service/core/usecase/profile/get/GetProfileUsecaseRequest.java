package com.learning.user_service.core.usecase.profile.get;

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
public class GetProfileUsecaseRequest implements UsecaseRequest {

    private UUID userId;
}
