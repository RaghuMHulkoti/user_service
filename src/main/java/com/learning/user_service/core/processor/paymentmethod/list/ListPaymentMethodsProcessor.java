package com.learning.user_service.core.processor.paymentmethod.list;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.paymentmethod.list.ListPaymentMethodsUsecase;
import com.learning.user_service.core.usecase.paymentmethod.list.ListPaymentMethodsUsecaseRequest;
import com.learning.user_service.core.usecase.paymentmethod.list.ListPaymentMethodsUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ListPaymentMethodsProcessor implements Processor<ListPaymentMethodsProcessorRequest, ListPaymentMethodsProcessorResponse> {

    private final ListPaymentMethodsUsecase listPaymentMethodsUsecase;

    public ListPaymentMethodsProcessor(ListPaymentMethodsUsecase listPaymentMethodsUsecase) {
        this.listPaymentMethodsUsecase = listPaymentMethodsUsecase;
    }

    @Override
    public ListPaymentMethodsProcessorResponse process(ListPaymentMethodsProcessorRequest request) {
        ListPaymentMethodsUsecaseResponse response = listPaymentMethodsUsecase.execute(buildListPaymentMethodsUsecaseRequest(request));
        return Optional.of(response)
                .map(ListPaymentMethodsProcessor::buildListPaymentMethodsProcessorResponse)
                .orElseThrow();
    }

    private static ListPaymentMethodsUsecaseRequest buildListPaymentMethodsUsecaseRequest(ListPaymentMethodsProcessorRequest request) {
        return ListPaymentMethodsUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ListPaymentMethodsProcessorResponse buildListPaymentMethodsProcessorResponse(ListPaymentMethodsUsecaseResponse response) {
        return ListPaymentMethodsProcessorResponse.builder()
                .methods(response.getMethods())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
