package com.learning.user_service.core.usecase.consent.current;

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
public class GetCurrentConsentsUsecaseRequest implements UsecaseRequest {

    private UUID userId;
}
