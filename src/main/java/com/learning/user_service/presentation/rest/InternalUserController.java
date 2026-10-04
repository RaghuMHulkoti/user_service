package com.learning.user_service.presentation.rest;

import com.learning.user_service.application.service.UserProfile;
import com.learning.user_service.application.service.UserProfileQueryService;
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

    private final UserProfileQueryService profileQueryService;

    public InternalUserController(UserProfileQueryService profileQueryService) {
        this.profileQueryService = profileQueryService;
    }

    @Operation(summary = "Internal user lookup (cached)")
    @GetMapping("/{id}")
    public UserProfile get(@PathVariable UUID id) {
        return profileQueryService.getProfile(id);
    }
}
