package com.learning.user_service.core.usecase.address.list;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAddressesUsecaseResponse implements UsecaseResponse {

    private List<AddressEntity> addresses;
}
