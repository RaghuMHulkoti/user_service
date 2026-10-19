package com.learning.user_service.core.processor.profile.delete;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.profile.delete.DeleteAccountUsecase;
import com.learning.user_service.core.usecase.profile.delete.DeleteAccountUsecaseRequest;
import com.learning.user_service.core.usecase.profile.delete.DeleteAccountUsecaseResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DeleteAccountProcessor implements Processor<DeleteAccountProcessorRequest, DeleteAccountProcessorResponse> {

    private final DeleteAccountUsecase deleteAccountUsecase;

    public DeleteAccountProcessor(DeleteAccountUsecase deleteAccountUsecase) {
        this.deleteAccountUsecase = deleteAccountUsecase;
    }

    @Override
    public DeleteAccountProcessorResponse process(DeleteAccountProcessorRequest request) {
        DeleteAccountUsecaseResponse deleteAccountUsecaseResponse = deleteAccountUsecase.execute(buildDeleteAccountUsecaseRequest(request));
        return Optional.of(deleteAccountUsecaseResponse)
                .map(DeleteAccountProcessor::buildDeleteAccountProcessorResponse)
                .orElseThrow();
    }

    private static DeleteAccountUsecaseRequest buildDeleteAccountUsecaseRequest(DeleteAccountProcessorRequest request) {
        return DeleteAccountUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static DeleteAccountProcessorResponse buildDeleteAccountProcessorResponse(DeleteAccountUsecaseResponse response) {
        return DeleteAccountProcessorResponse.builder()
                .success(response.isSuccess())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
