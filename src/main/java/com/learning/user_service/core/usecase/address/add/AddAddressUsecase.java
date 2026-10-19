package com.learning.user_service.core.usecase.address.add;

import com.learning.user_service.core.service.address.add.AddAddressService;
import com.learning.user_service.core.service.address.add.AddAddressServiceRequest;
import com.learning.user_service.core.service.address.add.AddAddressServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddAddressUsecase implements Usecase<AddAddressUsecaseRequest, AddAddressUsecaseResponse> {

    private final AddAddressService addAddressService;

    public AddAddressUsecase(AddAddressService addAddressService) {
        this.addAddressService = addAddressService;
    }

    @Override
    public AddAddressUsecaseResponse execute(AddAddressUsecaseRequest request) {
        AddAddressServiceResponse response = addAddressService.execute(buildAddAddressServiceRequest(request));
        return Optional.of(response)
                .map(AddAddressUsecase::buildAddAddressUsecaseResponse)
                .orElseThrow();
    }

    private static AddAddressServiceRequest buildAddAddressServiceRequest(AddAddressUsecaseRequest request) {
        return AddAddressServiceRequest.builder()
                .userId(request.getUserId())
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.getIsDefault())
                .geoLat(request.getGeoLat())
                .geoLng(request.getGeoLng())
                .build();
    }

    private static AddAddressUsecaseResponse buildAddAddressUsecaseResponse(AddAddressServiceResponse response) {
        return AddAddressUsecaseResponse.builder()
                .address(response.getAddress())
                .build();
    }
}
