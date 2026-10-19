package com.learning.user_service.core.processor.admin.suspend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.processor.ProcessorResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserProcessorResponse implements ProcessorResponse {

    private UserProfile profile;
    private StatusCode statusCode;
}
