package com.learning.user_service.core.service.address.update;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.AddressEventRecorder;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.gateway.AddressGateway;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateAddressService implements Service<UpdateAddressServiceRequest, UpdateAddressServiceResponse> {

    private final AddressGateway addressGateway;
    private final UserAccess userAccess;
    private final AddressEventRecorder addressEventRecorder;

    public UpdateAddressService(AddressGateway addressGateway, UserAccess userAccess, AddressEventRecorder addressEventRecorder) {
        this.addressGateway = addressGateway;
        this.userAccess = userAccess;
        this.addressEventRecorder = addressEventRecorder;
    }

    @Override
    @Transactional
    public UpdateAddressServiceResponse execute(UpdateAddressServiceRequest request) {
        UUID userId = request.getUserId();
        UUID addressId = request.getAddressId();
        userAccess.loadUsable(userId);
        AddressEntity address = findOwned(userId, addressId);
        if (request.getVersion() != null && request.getVersion() != address.getVersion()) {
            throw new ObjectOptimisticLockingFailureException(AddressEntity.class, addressId);
        }
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.isDefaultAddress()) {
            addressGateway.clearDefault(userId); // clears the persistence context
            address = findOwned(userId, addressId);
        }
        if (request.getLabel() != null) address.setLabel(request.getLabel());
        if (request.getLine1() != null) address.setLine1(request.getLine1().trim());
        if (request.getLine2() != null) address.setLine2(request.getLine2());
        if (request.getCity() != null) address.setCity(request.getCity().trim());
        if (request.getState() != null) address.setState(request.getState().trim());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode().trim());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getIsDefault() != null) address.setDefaultAddress(request.getIsDefault());
        if (request.getGeoLat() != null) address.setGeoLat(request.getGeoLat());
        if (request.getGeoLng() != null) address.setGeoLng(request.getGeoLng());
        address = addressGateway.saveAndFlush(address);

        addressEventRecorder.record(userId, address, "UPDATED");
        return UpdateAddressServiceResponse.builder().address(address).build();
    }

    private AddressEntity findOwned(UUID userId, UUID addressId) {
        return addressGateway.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address"));
    }
}
