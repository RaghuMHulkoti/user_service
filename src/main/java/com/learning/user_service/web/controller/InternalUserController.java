package com.learning.user_service.web.controller;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.processor.profile.get.GetProfileProcessor;
import com.learning.user_service.web.mapper.ProfileMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Service-to-service lookup. Only reachable inside the mesh; needs {@code X-Internal-Token} here. */
@RestController
@RequestMapping("/api/v1/internal/users")
@Tag(name = "Internal")
public class InternalUserController {

    private final GetProfileProcessor getProfileProcessor;

    public InternalUserController(GetProfileProcessor getProfileProcessor) {
        this.getProfileProcessor = getProfileProcessor;
    }

    @Operation(summary = "Internal user lookup (cached)")
    @GetMapping("/{id}")
    public UserProfile get(@PathVariable UUID id) {
        return ProfileMapper.mapGetProfileResponse(getProfileProcessor.process(ProfileMapper.toGetProfileRequest(id)));
    }
}
