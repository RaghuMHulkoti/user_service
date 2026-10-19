package com.learning.user_service.core.service.address.list;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.gateway.AddressGateway;
import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListAddressesService implements Service<ListAddressesServiceRequest, ListAddressesServiceResponse> {

    private final AddressGateway addressGateway;
    private final UserAccess userAccess;

    public ListAddressesService(AddressGateway addressGateway, UserAccess userAccess) {
        this.addressGateway = addressGateway;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional(readOnly = true)
    public ListAddressesServiceResponse execute(ListAddressesServiceRequest request) {
        userAccess.loadUsable(request.getUserId());
        return ListAddressesServiceResponse.builder()
                .addresses(addressGateway.findByUserId(request.getUserId()))
                .build();
    }
}
