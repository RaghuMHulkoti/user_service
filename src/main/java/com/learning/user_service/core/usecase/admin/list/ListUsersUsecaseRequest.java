package com.learning.user_service.core.usecase.admin.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.usecase.UsecaseRequest;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUsersUsecaseRequest implements UsecaseRequest {

    private UserStatus status;
    private Pageable pageable;
}
