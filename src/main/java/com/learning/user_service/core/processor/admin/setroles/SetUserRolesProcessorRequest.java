package com.learning.user_service.core.processor.admin.setroles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.core.processor.ProcessorRequest;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetUserRolesProcessorRequest implements ProcessorRequest {

    private UUID userId;
    private Set<RoleCode> roles;
}
