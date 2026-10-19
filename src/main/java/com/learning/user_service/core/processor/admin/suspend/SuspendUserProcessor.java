package com.learning.user_service.core.processor.admin.suspend;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.admin.suspend.SuspendUserUsecase;
import com.learning.user_service.core.usecase.admin.suspend.SuspendUserUsecaseRequest;
import com.learning.user_service.core.usecase.admin.suspend.SuspendUserUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SuspendUserProcessor implements Processor<SuspendUserProcessorRequest, SuspendUserProcessorResponse> {

    private final SuspendUserUsecase suspendUserUsecase;

    public SuspendUserProcessor(SuspendUserUsecase suspendUserUsecase) {
        this.suspendUserUsecase = suspendUserUsecase;
    }

    @Override
    public SuspendUserProcessorResponse process(SuspendUserProcessorRequest request) {
        SuspendUserUsecaseResponse usecaseResponse = suspendUserUsecase.execute(buildSuspendUserUsecaseRequest(request));
        return Optional.of(usecaseResponse)
                .map(SuspendUserProcessor::buildSuspendUserProcessorResponse)
                .orElseThrow();
    }

    private static SuspendUserUsecaseRequest buildSuspendUserUsecaseRequest(SuspendUserProcessorRequest request) {
        return SuspendUserUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static SuspendUserProcessorResponse buildSuspendUserProcessorResponse(SuspendUserUsecaseResponse response) {
        return SuspendUserProcessorResponse.builder()
                .profile(response.getProfile())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
