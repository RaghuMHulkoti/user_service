package com.learning.user_service.core.usecase.address.delete;

import com.learning.user_service.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAddressUsecaseResponse implements UsecaseResponse {

    private boolean success;
}
