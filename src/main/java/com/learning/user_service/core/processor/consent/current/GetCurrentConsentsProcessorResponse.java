package com.learning.user_service.core.processor.consent.current;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCurrentConsentsProcessorResponse implements ProcessorResponse {

    private List<ConsentLogEntity> consents;
    private StatusCode statusCode;
}
