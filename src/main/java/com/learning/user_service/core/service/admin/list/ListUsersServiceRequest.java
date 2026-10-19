package com.learning.user_service.core.service.admin.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.service.ServiceRequest;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUsersServiceRequest implements ServiceRequest {

    private UserStatus status;
    private Pageable pageable;
}
