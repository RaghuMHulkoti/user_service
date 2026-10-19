package com.learning.user_service.core.service.consent.current;

import com.learning.user_service.core.service.ServiceResponse;
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
public class GetCurrentConsentsServiceResponse implements ServiceResponse {

    private List<ConsentLogEntity> consents;
}
