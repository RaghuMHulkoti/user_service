package com.learning.user_service.presentation.rest;

import com.learning.user_service.application.command.UpdateRolesCommand;
import com.learning.user_service.application.query.PageView;
import com.learning.user_service.application.service.AdminUserService;
import com.learning.user_service.application.service.UserProfile;
import com.learning.user_service.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Operator endpoints (ADMIN, OPS). Offset pagination, as the TDD prescribes for admin lists. */
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "List users, optionally by status")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPS')")
    public PageView<UserProfile> list(@RequestParam(required = false) UserStatus status,
                                      @PageableDefault(size = 20, sort = "createdAt",
                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return adminUserService.list(status, pageable);
    }

    @Operation(summary = "Replace a user's roles")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfile setRoles(@PathVariable UUID id, @Valid @RequestBody UpdateRolesCommand cmd) {
        return adminUserService.setRoles(id, cmd.roles());
    }

    @Operation(summary = "Suspend a user (signs them out everywhere)")
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN','OPS')")
    public UserProfile suspend(@PathVariable UUID id) {
        return adminUserService.suspend(id);
    }

    @Operation(summary = "Reactivate a suspended user")
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN','OPS')")
    public UserProfile reactivate(@PathVariable UUID id) {
        return adminUserService.reactivate(id);
    }
}
