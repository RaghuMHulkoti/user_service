package com.learning.user_service.core.usecase.paymentmethod.list;

import com.learning.user_service.core.service.paymentmethod.list.ListPaymentMethodsService;
import com.learning.user_service.core.service.paymentmethod.list.ListPaymentMethodsServiceRequest;
import com.learning.user_service.core.service.paymentmethod.list.ListPaymentMethodsServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ListPaymentMethodsUsecase implements Usecase<ListPaymentMethodsUsecaseRequest, ListPaymentMethodsUsecaseResponse> {

    private final ListPaymentMethodsService listPaymentMethodsService;

    public ListPaymentMethodsUsecase(ListPaymentMethodsService listPaymentMethodsService) {
        this.listPaymentMethodsService = listPaymentMethodsService;
    }

    @Override
    public ListPaymentMethodsUsecaseResponse execute(ListPaymentMethodsUsecaseRequest request) {
        ListPaymentMethodsServiceResponse response = listPaymentMethodsService.execute(buildListPaymentMethodsServiceRequest(request));
        return Optional.of(response)
                .map(ListPaymentMethodsUsecase::buildListPaymentMethodsUsecaseResponse)
                .orElseThrow();
    }

    private static ListPaymentMethodsServiceRequest buildListPaymentMethodsServiceRequest(ListPaymentMethodsUsecaseRequest request) {
        return ListPaymentMethodsServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ListPaymentMethodsUsecaseResponse buildListPaymentMethodsUsecaseResponse(ListPaymentMethodsServiceResponse response) {
        return ListPaymentMethodsUsecaseResponse.builder()
                .methods(response.getMethods())
                .build();
    }
}
