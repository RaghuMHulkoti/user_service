package com.learning.user_service.core.processor.address.list;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAddressesProcessorResponse implements ProcessorResponse {

    private List<AddressEntity> addresses;
    private StatusCode statusCode;
}
