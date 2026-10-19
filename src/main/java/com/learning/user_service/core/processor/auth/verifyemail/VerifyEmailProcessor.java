package com.learning.user_service.core.processor.auth.verifyemail;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.verifyemail.VerifyEmailUsecase;
import com.learning.user_service.core.usecase.auth.verifyemail.VerifyEmailUsecaseRequest;
import com.learning.user_service.core.usecase.auth.verifyemail.VerifyEmailUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VerifyEmailProcessor implements Processor<VerifyEmailProcessorRequest, VerifyEmailProcessorResponse> {

    private final VerifyEmailUsecase verifyEmailUsecase;

    public VerifyEmailProcessor(VerifyEmailUsecase verifyEmailUsecase) {
        this.verifyEmailUsecase = verifyEmailUsecase;
    }

    @Override
    public VerifyEmailProcessorResponse process(VerifyEmailProcessorRequest request) {
        VerifyEmailUsecaseResponse usecaseResponse = verifyEmailUsecase.execute(buildVerifyEmailUsecaseRequest(request));
        return Optional.of(usecaseResponse)
                .map(VerifyEmailProcessor::buildVerifyEmailProcessorResponse)
                .orElseThrow();
    }

    private static VerifyEmailUsecaseRequest buildVerifyEmailUsecaseRequest(VerifyEmailProcessorRequest request) {
        return VerifyEmailUsecaseRequest.builder()
                .token(request.getToken())
                .build();
    }

    private static VerifyEmailProcessorResponse buildVerifyEmailProcessorResponse(VerifyEmailUsecaseResponse response) {
        return VerifyEmailProcessorResponse.builder()
                .success(response.isSuccess())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
