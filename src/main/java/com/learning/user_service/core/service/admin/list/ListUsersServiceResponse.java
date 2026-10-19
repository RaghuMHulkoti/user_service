package com.learning.user_service.core.service.admin.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.service.ServiceResponse;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUsersServiceResponse implements ServiceResponse {

    private Page<UserProfile> page;
}
