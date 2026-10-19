package com.learning.user_service.core.usecase.address.update;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressUsecaseResponse implements UsecaseResponse {

    private AddressEntity address;
}
