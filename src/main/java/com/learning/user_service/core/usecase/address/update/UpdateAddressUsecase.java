package com.learning.user_service.core.usecase.address.update;

import com.learning.user_service.core.service.address.update.UpdateAddressService;
import com.learning.user_service.core.service.address.update.UpdateAddressServiceRequest;
import com.learning.user_service.core.service.address.update.UpdateAddressServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UpdateAddressUsecase implements Usecase<UpdateAddressUsecaseRequest, UpdateAddressUsecaseResponse> {

    private final UpdateAddressService updateAddressService;

    public UpdateAddressUsecase(UpdateAddressService updateAddressService) {
        this.updateAddressService = updateAddressService;
    }

    @Override
    public UpdateAddressUsecaseResponse execute(UpdateAddressUsecaseRequest request) {
        UpdateAddressServiceResponse response = updateAddressService.execute(buildUpdateAddressServiceRequest(request));
        return Optional.of(response)
                .map(UpdateAddressUsecase::buildUpdateAddressUsecaseResponse)
                .orElseThrow();
    }

    private static UpdateAddressServiceRequest buildUpdateAddressServiceRequest(UpdateAddressUsecaseRequest request) {
        return UpdateAddressServiceRequest.builder()
                .userId(request.getUserId())
                .addressId(request.getAddressId())
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
                .version(request.getVersion())
                .build();
    }

    private static UpdateAddressUsecaseResponse buildUpdateAddressUsecaseResponse(UpdateAddressServiceResponse response) {
        return UpdateAddressUsecaseResponse.builder()
                .address(response.getAddress())
                .build();
    }
}
