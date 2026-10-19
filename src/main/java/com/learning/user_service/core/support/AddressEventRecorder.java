package com.learning.user_service.core.support;

import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Records the ADDRESS_UPDATED domain event shared by the address operations. */
@Component
public class AddressEventRecorder {

    private final DomainEventRecorder events;

    public AddressEventRecorder(DomainEventRecorder events) {
        this.events = events;
    }

    public void record(UUID userId, AddressEntity address, String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("addressId", address.getId().toString());
        data.put("action", action);
        data.put("isDefault", address.isDefaultAddress());
        data.put("city", address.getCity());
        data.put("postalCode", address.getPostalCode());
        data.put("country", address.getCountry());
        events.record(userId, UserEventType.ADDRESS_UPDATED, data);
    }
}
