package com.learning.user_service.core.usecase.admin.list;

import com.learning.user_service.core.service.admin.list.ListUsersService;
import com.learning.user_service.core.service.admin.list.ListUsersServiceRequest;
import com.learning.user_service.core.service.admin.list.ListUsersServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ListUsersUsecase implements Usecase<ListUsersUsecaseRequest, ListUsersUsecaseResponse> {

    private final ListUsersService listUsersService;

    public ListUsersUsecase(ListUsersService listUsersService) {
        this.listUsersService = listUsersService;
    }

    @Override
    public ListUsersUsecaseResponse execute(ListUsersUsecaseRequest request) {
        ListUsersServiceResponse serviceResponse = listUsersService.execute(buildListUsersServiceRequest(request));
        return Optional.of(serviceResponse)
                .map(ListUsersUsecase::buildListUsersUsecaseResponse)
                .orElseThrow();
    }

    private static ListUsersServiceRequest buildListUsersServiceRequest(ListUsersUsecaseRequest request) {
        return ListUsersServiceRequest.builder()
                .status(request.getStatus())
                .pageable(request.getPageable())
                .build();
    }

    private static ListUsersUsecaseResponse buildListUsersUsecaseResponse(ListUsersServiceResponse response) {
        return ListUsersUsecaseResponse.builder()
                .page(response.getPage())
                .build();
    }
}
