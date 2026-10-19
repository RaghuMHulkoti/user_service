package com.learning.user_service.core.service.address.delete;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.AddressEventRecorder;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.gateway.AddressGateway;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteAddressService implements Service<DeleteAddressServiceRequest, DeleteAddressServiceResponse> {

    private final AddressGateway addressGateway;
    private final UserAccess userAccess;
    private final AddressEventRecorder addressEventRecorder;

    public DeleteAddressService(AddressGateway addressGateway, UserAccess userAccess, AddressEventRecorder addressEventRecorder) {
        this.addressGateway = addressGateway;
        this.userAccess = userAccess;
        this.addressEventRecorder = addressEventRecorder;
    }

    @Override
    @Transactional
    public DeleteAddressServiceResponse execute(DeleteAddressServiceRequest request) {
        UUID userId = request.getUserId();
        UUID addressId = request.getAddressId();
        userAccess.loadUsable(userId);
        AddressEntity address = findOwned(userId, addressId);
        boolean wasDefault = address.isDefaultAddress();
        addressGateway.delete(address);
        addressGateway.flush();
        addressEventRecorder.record(userId, address, "DELETED");

        if (wasDefault) {
            addressGateway.findFirstByUserId(userId).ifPresent(next -> {
                next.setDefaultAddress(true);
                addressEventRecorder.record(userId, next, "DEFAULT_CHANGED");
            });
        }
        return DeleteAddressServiceResponse.builder().success(true).build();
    }

    private AddressEntity findOwned(UUID userId, UUID addressId) {
        return addressGateway.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> ApiException.notFound("Address"));
    }
}
