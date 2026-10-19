package com.learning.user_service.core.processor.address.add;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.address.add.AddAddressUsecase;
import com.learning.user_service.core.usecase.address.add.AddAddressUsecaseRequest;
import com.learning.user_service.core.usecase.address.add.AddAddressUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddAddressProcessor implements Processor<AddAddressProcessorRequest, AddAddressProcessorResponse> {

    private final AddAddressUsecase addAddressUsecase;

    public AddAddressProcessor(AddAddressUsecase addAddressUsecase) {
        this.addAddressUsecase = addAddressUsecase;
    }

    @Override
    public AddAddressProcessorResponse process(AddAddressProcessorRequest request) {
        AddAddressUsecaseResponse response = addAddressUsecase.execute(buildAddAddressUsecaseRequest(request));
        return Optional.of(response)
                .map(AddAddressProcessor::buildAddAddressProcessorResponse)
                .orElseThrow();
    }

    private static AddAddressUsecaseRequest buildAddAddressUsecaseRequest(AddAddressProcessorRequest request) {
        return AddAddressUsecaseRequest.builder()
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

    private static AddAddressProcessorResponse buildAddAddressProcessorResponse(AddAddressUsecaseResponse response) {
        return AddAddressProcessorResponse.builder()
                .address(response.getAddress())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
