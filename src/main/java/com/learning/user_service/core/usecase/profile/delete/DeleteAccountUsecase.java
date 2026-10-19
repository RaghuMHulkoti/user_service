package com.learning.user_service.core.usecase.profile.delete;

import com.learning.user_service.core.service.profile.delete.DeleteAccountService;
import com.learning.user_service.core.service.profile.delete.DeleteAccountServiceRequest;
import com.learning.user_service.core.service.profile.delete.DeleteAccountServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DeleteAccountUsecase implements Usecase<DeleteAccountUsecaseRequest, DeleteAccountUsecaseResponse> {

    private final DeleteAccountService deleteAccountService;

    public DeleteAccountUsecase(DeleteAccountService deleteAccountService) {
        this.deleteAccountService = deleteAccountService;
    }

    @Override
    public DeleteAccountUsecaseResponse execute(DeleteAccountUsecaseRequest request) {
        DeleteAccountServiceResponse deleteAccountServiceResponse = deleteAccountService.execute(buildDeleteAccountServiceRequest(request));
        return Optional.of(deleteAccountServiceResponse)
                .map(DeleteAccountUsecase::buildDeleteAccountUsecaseResponse)
                .orElseThrow();
    }

    private static DeleteAccountServiceRequest buildDeleteAccountServiceRequest(DeleteAccountUsecaseRequest request) {
        return DeleteAccountServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static DeleteAccountUsecaseResponse buildDeleteAccountUsecaseResponse(DeleteAccountServiceResponse response) {
        return DeleteAccountUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
