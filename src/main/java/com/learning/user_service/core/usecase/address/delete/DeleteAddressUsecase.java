package com.learning.user_service.core.usecase.address.delete;

import com.learning.user_service.core.service.address.delete.DeleteAddressService;
import com.learning.user_service.core.service.address.delete.DeleteAddressServiceRequest;
import com.learning.user_service.core.service.address.delete.DeleteAddressServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeleteAddressUsecase implements Usecase<DeleteAddressUsecaseRequest, DeleteAddressUsecaseResponse> {

    private final DeleteAddressService deleteAddressService;

    public DeleteAddressUsecase(DeleteAddressService deleteAddressService) {
        this.deleteAddressService = deleteAddressService;
    }

    @Override
    public DeleteAddressUsecaseResponse execute(DeleteAddressUsecaseRequest request) {
        DeleteAddressServiceResponse response = deleteAddressService.execute(buildDeleteAddressServiceRequest(request));
        return Optional.of(response)
                .map(DeleteAddressUsecase::buildDeleteAddressUsecaseResponse)
                .orElseThrow();
    }

    private static DeleteAddressServiceRequest buildDeleteAddressServiceRequest(DeleteAddressUsecaseRequest request) {
        return DeleteAddressServiceRequest.builder()
                .userId(request.getUserId())
                .addressId(request.getAddressId())
                .build();
    }

    private static DeleteAddressUsecaseResponse buildDeleteAddressUsecaseResponse(DeleteAddressServiceResponse response) {
        return DeleteAddressUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
