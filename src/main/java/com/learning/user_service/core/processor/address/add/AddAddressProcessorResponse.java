package com.learning.user_service.core.processor.address.add;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAddressProcessorResponse implements ProcessorResponse {

    private AddressEntity address;
    private StatusCode statusCode;
}
