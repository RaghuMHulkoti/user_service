package com.learning.user_service.web.response;

import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AddressResponse(UUID id, String label, String line1, String line2, String city, String state,
                          String postalCode, String country, boolean isDefault, BigDecimal geoLat,
                          BigDecimal geoLng, long version, Instant createdAt, Instant updatedAt) {

    public static AddressResponse from(AddressEntity a) {
        return new AddressResponse(a.getId(), a.getLabel(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.isDefaultAddress(), a.getGeoLat(), a.getGeoLng(),
                a.getVersion(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
