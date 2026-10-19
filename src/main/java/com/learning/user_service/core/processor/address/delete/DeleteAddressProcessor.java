package com.learning.user_service.core.processor.address.delete;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.address.delete.DeleteAddressUsecase;
import com.learning.user_service.core.usecase.address.delete.DeleteAddressUsecaseRequest;
import com.learning.user_service.core.usecase.address.delete.DeleteAddressUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeleteAddressProcessor implements Processor<DeleteAddressProcessorRequest, DeleteAddressProcessorResponse> {

    private final DeleteAddressUsecase deleteAddressUsecase;

    public DeleteAddressProcessor(DeleteAddressUsecase deleteAddressUsecase) {
        this.deleteAddressUsecase = deleteAddressUsecase;
    }

    @Override
    public DeleteAddressProcessorResponse process(DeleteAddressProcessorRequest request) {
        DeleteAddressUsecaseResponse response = deleteAddressUsecase.execute(buildDeleteAddressUsecaseRequest(request));
        return Optional.of(response)
                .map(DeleteAddressProcessor::buildDeleteAddressProcessorResponse)
                .orElseThrow();
    }

    private static DeleteAddressUsecaseRequest buildDeleteAddressUsecaseRequest(DeleteAddressProcessorRequest request) {
        return DeleteAddressUsecaseRequest.builder()
                .userId(request.getUserId())
                .addressId(request.getAddressId())
                .build();
    }

    private static DeleteAddressProcessorResponse buildDeleteAddressProcessorResponse(DeleteAddressUsecaseResponse response) {
        return DeleteAddressProcessorResponse.builder()
                .success(response.isSuccess())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
