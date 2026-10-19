package com.learning.user_service.core.usecase.consent.record;

import com.learning.user_service.core.usecase.UsecaseResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordConsentUsecaseResponse implements UsecaseResponse {

    private ConsentLogEntity consent;
}
