package com.learning.user_service.core.processor.address.delete;

import com.learning.user_service.core.processor.ProcessorRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAddressProcessorRequest implements ProcessorRequest {

    private UUID userId;
    private UUID addressId;
}
