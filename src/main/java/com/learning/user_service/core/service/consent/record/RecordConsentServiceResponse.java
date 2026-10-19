package com.learning.user_service.core.service.consent.record;

import com.learning.user_service.core.service.ServiceResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordConsentServiceResponse implements ServiceResponse {

    private ConsentLogEntity consent;
}
