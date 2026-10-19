package com.learning.user_service.core.processor.profile.delete;

import com.learning.user_service.core.processor.ProcessorRequest;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountProcessorRequest implements ProcessorRequest {

    private UUID userId;
}
