package com.learning.user_service.core.service.paymentmethod.list;

import com.learning.user_service.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPaymentMethodsServiceRequest implements ServiceRequest {

    private UUID userId;
}
