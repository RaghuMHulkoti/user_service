package com.learning.user_service.application.query;

import com.learning.user_service.infrastructure.persistence.entity.AddressEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AddressView(UUID id, String label, String line1, String line2, String city, String state,
                          String postalCode, String country, boolean isDefault, BigDecimal geoLat,
                          BigDecimal geoLng, long version, Instant createdAt, Instant updatedAt) {

    public static AddressView from(AddressEntity a) {
        return new AddressView(a.getId(), a.getLabel(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                a.getPostalCode(), a.getCountry(), a.isDefaultAddress(), a.getGeoLat(), a.getGeoLng(),
                a.getVersion(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
