package com.learning.user_service.core.processor.admin.suspend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.core.processor.ProcessorRequest;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserProcessorRequest implements ProcessorRequest {

    private UUID userId;
}
