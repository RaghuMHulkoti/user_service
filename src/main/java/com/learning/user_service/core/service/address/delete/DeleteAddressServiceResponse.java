package com.learning.user_service.core.service.address.delete;

import com.learning.user_service.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAddressServiceResponse implements ServiceResponse {

    private boolean success;
}
