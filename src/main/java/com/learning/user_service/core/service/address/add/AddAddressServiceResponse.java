package com.learning.user_service.core.service.address.add;

import com.learning.user_service.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAddressServiceResponse implements ServiceResponse {

    private AddressEntity address;
}
