package com.learning.user_service.core.processor.address.list;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.address.list.ListAddressesUsecase;
import com.learning.user_service.core.usecase.address.list.ListAddressesUsecaseRequest;
import com.learning.user_service.core.usecase.address.list.ListAddressesUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ListAddressesProcessor implements Processor<ListAddressesProcessorRequest, ListAddressesProcessorResponse> {

    private final ListAddressesUsecase listAddressesUsecase;

    public ListAddressesProcessor(ListAddressesUsecase listAddressesUsecase) {
        this.listAddressesUsecase = listAddressesUsecase;
    }

    @Override
    public ListAddressesProcessorResponse process(ListAddressesProcessorRequest request) {
        ListAddressesUsecaseResponse response = listAddressesUsecase.execute(buildListAddressesUsecaseRequest(request));
        return Optional.of(response)
                .map(ListAddressesProcessor::buildListAddressesProcessorResponse)
                .orElseThrow();
    }

    private static ListAddressesUsecaseRequest buildListAddressesUsecaseRequest(ListAddressesProcessorRequest request) {
        return ListAddressesUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ListAddressesProcessorResponse buildListAddressesProcessorResponse(ListAddressesUsecaseResponse response) {
        return ListAddressesProcessorResponse.builder()
                .addresses(response.getAddresses())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
