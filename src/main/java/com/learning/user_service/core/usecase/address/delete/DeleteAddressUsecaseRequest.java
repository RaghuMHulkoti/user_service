package com.learning.user_service.core.usecase.address.delete;

import com.learning.user_service.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAddressUsecaseRequest implements UsecaseRequest {

    private UUID userId;
    private UUID addressId;
}
