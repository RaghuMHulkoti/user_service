package com.learning.user_service.web.mapper;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.processor.profile.delete.DeleteAccountProcessorRequest;
import com.learning.user_service.core.processor.profile.get.GetProfileProcessorRequest;
import com.learning.user_service.core.processor.profile.get.GetProfileProcessorResponse;
import com.learning.user_service.core.processor.profile.update.UpdateProfileProcessorRequest;
import com.learning.user_service.core.processor.profile.update.UpdateProfileProcessorResponse;
import com.learning.user_service.web.model.UpdateProfileWebRequest;

import java.util.UUID;

public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static GetProfileProcessorRequest toGetProfileRequest(UUID userId) {
        return GetProfileProcessorRequest.builder().userId(userId).build();
    }

    public static UserProfile mapGetProfileResponse(GetProfileProcessorResponse response) {
        return response.getProfile();
    }

    public static UpdateProfileProcessorRequest toUpdateProfileRequest(UUID userId, UpdateProfileWebRequest request) {
        return UpdateProfileProcessorRequest.builder()
                .userId(userId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .locale(request.locale())
                .version(request.version())
                .build();
    }

    public static UserProfile mapUpdateProfileResponse(UpdateProfileProcessorResponse response) {
        return response.getProfile();
    }

    public static DeleteAccountProcessorRequest toDeleteAccountRequest(UUID userId) {
        return DeleteAccountProcessorRequest.builder().userId(userId).build();
    }
}
