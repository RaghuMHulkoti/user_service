package com.learning.user_service.core.processor.admin.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.processor.ProcessorRequest;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListUsersProcessorRequest implements ProcessorRequest {

    private UserStatus status;
    private Pageable pageable;
}
