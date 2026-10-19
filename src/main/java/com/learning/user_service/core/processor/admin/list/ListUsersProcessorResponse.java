package com.learning.user_service.core.processor.admin.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.processor.ProcessorResponse;
import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUsersProcessorResponse implements ProcessorResponse {

    private Page<UserProfile> page;
    private StatusCode statusCode;
}
