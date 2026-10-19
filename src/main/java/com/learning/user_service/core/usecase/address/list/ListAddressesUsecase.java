package com.learning.user_service.core.usecase.address.list;

import com.learning.user_service.core.service.address.list.ListAddressesService;
import com.learning.user_service.core.service.address.list.ListAddressesServiceRequest;
import com.learning.user_service.core.service.address.list.ListAddressesServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ListAddressesUsecase implements Usecase<ListAddressesUsecaseRequest, ListAddressesUsecaseResponse> {

    private final ListAddressesService listAddressesService;

    public ListAddressesUsecase(ListAddressesService listAddressesService) {
        this.listAddressesService = listAddressesService;
    }

    @Override
    public ListAddressesUsecaseResponse execute(ListAddressesUsecaseRequest request) {
        ListAddressesServiceResponse response = listAddressesService.execute(buildListAddressesServiceRequest(request));
        return Optional.of(response)
                .map(ListAddressesUsecase::buildListAddressesUsecaseResponse)
                .orElseThrow();
    }

    private static ListAddressesServiceRequest buildListAddressesServiceRequest(ListAddressesUsecaseRequest request) {
        return ListAddressesServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ListAddressesUsecaseResponse buildListAddressesUsecaseResponse(ListAddressesServiceResponse response) {
        return ListAddressesUsecaseResponse.builder()
                .addresses(response.getAddresses())
                .build();
    }
}
