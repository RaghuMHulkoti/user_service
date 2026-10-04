package com.learning.user_service.application.service;

import com.learning.user_service.application.command.AddAddressCommand;
import com.learning.user_service.application.command.UpdateAddressCommand;
import com.learning.user_service.application.query.AddressView;
import com.learning.user_service.domain.event.UserEventType;
import com.learning.user_service.domain.port.DomainEventRecorder;
import com.learning.user_service.infrastructure.persistence.entity.AddressEntity;
import com.learning.user_service.infrastructure.persistence.repository.AddressRepository;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Address book. At most one default address per user (enforced by a partial unique index).
 * The first address becomes the default automatically.
 */
@Service
public class AddressService {

    static final int MAX_ADDRESSES = 20;

    private final AddressRepository addressRepository;
    private final DomainEventRecorder events;
    private final UserAccess userAccess;

    public AddressService(AddressRepository addressRepository, DomainEventRecorder events, UserAccess userAccess) {
        this.addressRepository = addressRepository;
        this.events = events;
        this.userAccess = userAccess;
    }

    @Transactional(readOnly = true)
    public List<AddressView> list(UUID userId) {
        userAccess.loadUsable(userId);
        return addressRepository.findByUserIdOrderByCreatedAtAsc(userId).stream().map(AddressView::from).toList();
    }

    @Transactional
    public AddressView add(UUID userId, AddAddressCommand cmd) {
        userAccess.loadUsable(userId);
        long count = addressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Address book is full (max " + MAX_ADDRESSES + ")");
        }
        boolean makeDefault = Boolean.TRUE.equals(cmd.isDefault()) || count == 0;
        if (makeDefault) {
            // Must reach the DB before the INSERT, or the one-default index rejects it.
            addressRepository.clearDefault(userId);
        }
        AddressEntity address = new AddressEntity();
        address.setUserId(userId);
        address.setLabel(cmd.label());
        address.setLine1(cmd.line1().trim());
        address.setLine2(cmd.line2());
        address.setCity(cmd.city().trim());
        address.setState(cmd.state().trim());
        address.setPostalCode(cmd.postalCode().trim());
        address.setCountry(cmd.country());
        address.setDefaultAddress(makeDefault);
        address.setGeoLat(cmd.geoLat());
        address.setGeoLng(cmd.geoLng());
        address = addressRepository.saveAndFlush(address);

        recordEvent(userId, address, "CREATED");
        return AddressView.from(address);
    }

    @Transactional
    public AddressView update(UUID userId, UUID addressId, UpdateAddressCommand cmd) {
        userAccess.loadUsable(userId);
        AddressEntity address = findOwned(userId, addressId);
        if (cmd.version() != null && cmd.version() != address.getVersion()) {
            throw new ObjectOptimisticLockingFailureException(AddressEntity.class, addressId);
        }
        if (Boolean.TRUE.equals(cmd.isDefault()) && !address.isDefaultAddress()) {
            addressRepository.clearDefault(userId); // clears the persistence context
            address = findOwned(userId, addressId);
        }
        if (cmd.label() != null) address.setLabel(cmd.label());
        if (cmd.line1() != null) address.setLine1(cmd.line1().trim());
        if (cmd.line2() != null) address.setLine2(cmd.line2());
        if (cmd.city() != null) address.setCity(cmd.city().trim());
        if (cmd.state() != null) address.setState(cmd.state().trim());
        if (cmd.postalCode() != null) address.setPostalCode(cmd.postalCode().trim());
        if (cmd.country() != null) address.setCountry(cmd.country());
        if (cmd.isDefault() != null) address.setDefaultAddress(cmd.isDefault());
        if (cmd.geoLat() != null) address.setGeoLat(cmd.geoLat());
        if (cmd.geoLng() != null) address.setGeoLng(cmd.geoLng());
        address = addressRepository.saveAndFlush(address);

        recordEvent(userId, address, "UPDATED");
        return AddressView.from(address);
    }

    @Transactional
    public void delete(UUID userId, UUID addressId) {
        userAccess.loadUsable(userId);
        AddressEntity address = findOwned(userId, addressId);
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);
        addressRepository.flush();
        recordEvent(userId, address, "DELETED");

        if (wasDefault) {
            addressRepository.findFirstByUserIdOrderByCreatedAtAsc(userId).ifPresent(next -> {
                next.setDefaultAddress(true);
                recordEvent(userId, next, "DEFAULT_CHANGED");
            });
        }
    }

    private AddressEntity findOwned(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address"));
    }

    private void recordEvent(UUID userId, AddressEntity address, String action) {
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
