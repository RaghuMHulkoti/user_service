package com.learning.user_service.core.usecase.paymentmethod.list;

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
public class ListPaymentMethodsUsecaseRequest implements UsecaseRequest {

    private UUID userId;
}
