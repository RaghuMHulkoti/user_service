package com.learning.user_service.core.processor.admin.list;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.admin.list.ListUsersUsecase;
import com.learning.user_service.core.usecase.admin.list.ListUsersUsecaseRequest;
import com.learning.user_service.core.usecase.admin.list.ListUsersUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ListUsersProcessor implements Processor<ListUsersProcessorRequest, ListUsersProcessorResponse> {

    private final ListUsersUsecase listUsersUsecase;

    public ListUsersProcessor(ListUsersUsecase listUsersUsecase) {
        this.listUsersUsecase = listUsersUsecase;
    }

    @Override
    public ListUsersProcessorResponse process(ListUsersProcessorRequest request) {
        ListUsersUsecaseResponse usecaseResponse = listUsersUsecase.execute(buildListUsersUsecaseRequest(request));
        return Optional.of(usecaseResponse)
                .map(ListUsersProcessor::buildListUsersProcessorResponse)
                .orElseThrow();
    }

    private static ListUsersUsecaseRequest buildListUsersUsecaseRequest(ListUsersProcessorRequest request) {
        return ListUsersUsecaseRequest.builder()
                .status(request.getStatus())
                .pageable(request.getPageable())
                .build();
    }

    private static ListUsersProcessorResponse buildListUsersProcessorResponse(ListUsersUsecaseResponse response) {
        return ListUsersProcessorResponse.builder()
                .page(response.getPage())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
