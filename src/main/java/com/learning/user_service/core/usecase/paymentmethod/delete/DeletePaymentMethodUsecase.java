package com.learning.user_service.core.usecase.paymentmethod.delete;

import com.learning.user_service.core.service.paymentmethod.delete.DeletePaymentMethodService;
import com.learning.user_service.core.service.paymentmethod.delete.DeletePaymentMethodServiceRequest;
import com.learning.user_service.core.service.paymentmethod.delete.DeletePaymentMethodServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeletePaymentMethodUsecase implements Usecase<DeletePaymentMethodUsecaseRequest, DeletePaymentMethodUsecaseResponse> {

    private final DeletePaymentMethodService deletePaymentMethodService;

    public DeletePaymentMethodUsecase(DeletePaymentMethodService deletePaymentMethodService) {
        this.deletePaymentMethodService = deletePaymentMethodService;
    }

    @Override
    public DeletePaymentMethodUsecaseResponse execute(DeletePaymentMethodUsecaseRequest request) {
        DeletePaymentMethodServiceResponse response = deletePaymentMethodService.execute(buildDeletePaymentMethodServiceRequest(request));
        return Optional.of(response)
                .map(DeletePaymentMethodUsecase::buildDeletePaymentMethodUsecaseResponse)
                .orElseThrow();
    }

    private static DeletePaymentMethodServiceRequest buildDeletePaymentMethodServiceRequest(DeletePaymentMethodUsecaseRequest request) {
        return DeletePaymentMethodServiceRequest.builder()
                .userId(request.getUserId())
                .methodId(request.getMethodId())
                .build();
    }

    private static DeletePaymentMethodUsecaseResponse buildDeletePaymentMethodUsecaseResponse(DeletePaymentMethodServiceResponse response) {
        return DeletePaymentMethodUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
