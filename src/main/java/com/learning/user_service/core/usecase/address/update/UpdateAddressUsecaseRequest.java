package com.learning.user_service.core.usecase.address.update;

import com.learning.user_service.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressUsecaseRequest implements UsecaseRequest {

    private UUID userId;
    private UUID addressId;
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private BigDecimal geoLat;
    private BigDecimal geoLng;
    private Long version;
}
