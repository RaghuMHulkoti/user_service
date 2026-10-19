package com.learning.user_service.core.usecase.admin.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.usecase.UsecaseResponse;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUsersUsecaseResponse implements UsecaseResponse {

    private Page<UserProfile> page;
}
