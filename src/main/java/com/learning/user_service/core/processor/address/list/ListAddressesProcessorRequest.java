package com.learning.user_service.core.processor.address.list;

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
public class ListAddressesProcessorRequest implements ProcessorRequest {

    private UUID userId;
}
