package com.learning.user_service.core.processor.consent.record;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordConsentProcessorResponse implements ProcessorResponse {

    private ConsentLogEntity consent;
    private StatusCode statusCode;
}
