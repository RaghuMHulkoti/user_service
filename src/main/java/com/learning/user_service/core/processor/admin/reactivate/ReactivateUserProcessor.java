package com.learning.user_service.core.processor.admin.reactivate;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.admin.reactivate.ReactivateUserUsecase;
import com.learning.user_service.core.usecase.admin.reactivate.ReactivateUserUsecaseRequest;
import com.learning.user_service.core.usecase.admin.reactivate.ReactivateUserUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReactivateUserProcessor implements Processor<ReactivateUserProcessorRequest, ReactivateUserProcessorResponse> {

    private final ReactivateUserUsecase reactivateUserUsecase;

    public ReactivateUserProcessor(ReactivateUserUsecase reactivateUserUsecase) {
        this.reactivateUserUsecase = reactivateUserUsecase;
    }

    @Override
    public ReactivateUserProcessorResponse process(ReactivateUserProcessorRequest request) {
        ReactivateUserUsecaseResponse usecaseResponse = reactivateUserUsecase.execute(buildReactivateUserUsecaseRequest(request));
        return Optional.of(usecaseResponse)
                .map(ReactivateUserProcessor::buildReactivateUserProcessorResponse)
                .orElseThrow();
    }

    private static ReactivateUserUsecaseRequest buildReactivateUserUsecaseRequest(ReactivateUserProcessorRequest request) {
        return ReactivateUserUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ReactivateUserProcessorResponse buildReactivateUserProcessorResponse(ReactivateUserUsecaseResponse response) {
        return ReactivateUserProcessorResponse.builder()
                .profile(response.getProfile())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
