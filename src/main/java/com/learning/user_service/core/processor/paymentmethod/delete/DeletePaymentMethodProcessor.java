package com.learning.user_service.core.processor.paymentmethod.delete;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.paymentmethod.delete.DeletePaymentMethodUsecase;
import com.learning.user_service.core.usecase.paymentmethod.delete.DeletePaymentMethodUsecaseRequest;
import com.learning.user_service.core.usecase.paymentmethod.delete.DeletePaymentMethodUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeletePaymentMethodProcessor implements Processor<DeletePaymentMethodProcessorRequest, DeletePaymentMethodProcessorResponse> {

    private final DeletePaymentMethodUsecase deletePaymentMethodUsecase;

    public DeletePaymentMethodProcessor(DeletePaymentMethodUsecase deletePaymentMethodUsecase) {
        this.deletePaymentMethodUsecase = deletePaymentMethodUsecase;
    }

    @Override
    public DeletePaymentMethodProcessorResponse process(DeletePaymentMethodProcessorRequest request) {
        DeletePaymentMethodUsecaseResponse response = deletePaymentMethodUsecase.execute(buildDeletePaymentMethodUsecaseRequest(request));
        return Optional.of(response)
                .map(DeletePaymentMethodProcessor::buildDeletePaymentMethodProcessorResponse)
                .orElseThrow();
    }

    private static DeletePaymentMethodUsecaseRequest buildDeletePaymentMethodUsecaseRequest(DeletePaymentMethodProcessorRequest request) {
        return DeletePaymentMethodUsecaseRequest.builder()
                .userId(request.getUserId())
                .methodId(request.getMethodId())
                .build();
    }

    private static DeletePaymentMethodProcessorResponse buildDeletePaymentMethodProcessorResponse(DeletePaymentMethodUsecaseResponse response) {
        return DeletePaymentMethodProcessorResponse.builder()
                .success(response.isSuccess())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
