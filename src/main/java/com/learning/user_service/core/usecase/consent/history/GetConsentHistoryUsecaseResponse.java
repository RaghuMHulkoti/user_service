package com.learning.user_service.core.usecase.consent.history;

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
public class GetConsentHistoryUsecaseResponse implements UsecaseResponse {

    private List<ConsentLogEntity> consents;
}
