package com.learning.user_service.core.usecase.consent.current;

import com.learning.user_service.core.usecase.UsecaseResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCurrentConsentsUsecaseResponse implements UsecaseResponse {

    private List<ConsentLogEntity> consents;
}
