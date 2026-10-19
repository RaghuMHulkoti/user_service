package com.learning.user_service.core.processor.address.update;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.address.update.UpdateAddressUsecase;
import com.learning.user_service.core.usecase.address.update.UpdateAddressUsecaseRequest;
import com.learning.user_service.core.usecase.address.update.UpdateAddressUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UpdateAddressProcessor implements Processor<UpdateAddressProcessorRequest, UpdateAddressProcessorResponse> {

    private final UpdateAddressUsecase updateAddressUsecase;

    public UpdateAddressProcessor(UpdateAddressUsecase updateAddressUsecase) {
        this.updateAddressUsecase = updateAddressUsecase;
    }

    @Override
    public UpdateAddressProcessorResponse process(UpdateAddressProcessorRequest request) {
        UpdateAddressUsecaseResponse response = updateAddressUsecase.execute(buildUpdateAddressUsecaseRequest(request));
        return Optional.of(response)
                .map(UpdateAddressProcessor::buildUpdateAddressProcessorResponse)
                .orElseThrow();
    }

    private static UpdateAddressUsecaseRequest buildUpdateAddressUsecaseRequest(UpdateAddressProcessorRequest request) {
        return UpdateAddressUsecaseRequest.builder()
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

    private static UpdateAddressProcessorResponse buildUpdateAddressProcessorResponse(UpdateAddressUsecaseResponse response) {
        return UpdateAddressProcessorResponse.builder()
                .address(response.getAddress())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
