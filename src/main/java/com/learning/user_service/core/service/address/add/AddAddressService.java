package com.learning.user_service.core.service.address.add;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.AddressEventRecorder;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.AddressGateway;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Address book. At most one default address per user (enforced by a partial unique index).
 * The first address becomes the default automatically.
 */
@Component
public class AddAddressService implements Service<AddAddressServiceRequest, AddAddressServiceResponse> {

    static final int MAX_ADDRESSES = 20;

    private final AddressGateway addressGateway;
    private final UserAccess userAccess;
    private final AddressEventRecorder addressEventRecorder;

    public AddAddressService(AddressGateway addressGateway, UserAccess userAccess, AddressEventRecorder addressEventRecorder) {
        this.addressGateway = addressGateway;
        this.userAccess = userAccess;
        this.addressEventRecorder = addressEventRecorder;
    }

    @Override
    @Transactional
    public AddAddressServiceResponse execute(AddAddressServiceRequest request) {
        UUID userId = request.getUserId();
        userAccess.loadUsable(userId);
        long count = addressGateway.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Address book is full (max " + MAX_ADDRESSES + ")");
        }
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault()) || count == 0;
        if (makeDefault) {
            // Must reach the DB before the INSERT, or the one-default index rejects it.
            addressGateway.clearDefault(userId);
        }
        AddressEntity address = new AddressEntity();
        address.setUserId(userId);
        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1().trim());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPostalCode(request.getPostalCode().trim());
        address.setCountry(request.getCountry());
        address.setDefaultAddress(makeDefault);
        address.setGeoLat(request.getGeoLat());
        address.setGeoLng(request.getGeoLng());
        address = addressGateway.saveAndFlush(address);

        addressEventRecorder.record(userId, address, "CREATED");
        return AddAddressServiceResponse.builder().address(address).build();
    }
}
