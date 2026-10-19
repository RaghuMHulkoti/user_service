package com.learning.user_service.core.processor.profile.get;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProfileProcessorResponse implements ProcessorResponse {

    private UserProfile profile;
    private StatusCode statusCode;
}
