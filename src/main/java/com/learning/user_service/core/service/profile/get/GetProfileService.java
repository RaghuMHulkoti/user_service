package com.learning.user_service.core.service.profile.get;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.ProfileReader;
import org.springframework.stereotype.Component;

@Component
public class GetProfileService implements Service<GetProfileServiceRequest, GetProfileServiceResponse> {

    private final ProfileReader profileReader;

    public GetProfileService(ProfileReader profileReader) {
        this.profileReader = profileReader;
    }

    @Override
    public GetProfileServiceResponse execute(GetProfileServiceRequest request) {
        UserProfile profile = profileReader.getProfile(request.getUserId());
        return GetProfileServiceResponse.builder().profile(profile).build();
    }
}
