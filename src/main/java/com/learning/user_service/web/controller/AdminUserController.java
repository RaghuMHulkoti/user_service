package com.learning.user_service.web.controller;

import com.learning.user_service.web.model.UpdateRolesWebRequest;
import com.learning.user_service.web.response.PageResponse;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.processor.admin.list.ListUsersProcessor;
import com.learning.user_service.core.processor.admin.setroles.SetUserRolesProcessor;
import com.learning.user_service.core.processor.admin.suspend.SuspendUserProcessor;
import com.learning.user_service.core.processor.admin.reactivate.ReactivateUserProcessor;
import com.learning.user_service.web.mapper.AdminMapper;
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

    private final ListUsersProcessor listUsersProcessor;
    private final SetUserRolesProcessor setUserRolesProcessor;
    private final SuspendUserProcessor suspendUserProcessor;
    private final ReactivateUserProcessor reactivateUserProcessor;

    public AdminUserController(ListUsersProcessor listUsersProcessor, SetUserRolesProcessor setUserRolesProcessor,
                               SuspendUserProcessor suspendUserProcessor,
                               ReactivateUserProcessor reactivateUserProcessor) {
        this.listUsersProcessor = listUsersProcessor;
        this.setUserRolesProcessor = setUserRolesProcessor;
        this.suspendUserProcessor = suspendUserProcessor;
        this.reactivateUserProcessor = reactivateUserProcessor;
    }

    @Operation(summary = "List users, optionally by status")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPS')")
    public PageResponse<UserProfile> list(@RequestParam(required = false) UserStatus status,
                                      @PageableDefault(size = 20, sort = "createdAt",
                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return AdminMapper.mapListUsersResponse(listUsersProcessor.process(AdminMapper.toListUsersRequest(status, pageable)));
    }

    @Operation(summary = "Replace a user's roles")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfile setRoles(@PathVariable UUID id, @Valid @RequestBody UpdateRolesWebRequest cmd) {
        return AdminMapper.mapSetUserRolesResponse(
                setUserRolesProcessor.process(AdminMapper.toSetUserRolesRequest(id, cmd.roles())));
    }

    @Operation(summary = "Suspend a user (signs them out everywhere)")
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN','OPS')")
    public UserProfile suspend(@PathVariable UUID id) {
        return AdminMapper.mapSuspendUserResponse(suspendUserProcessor.process(AdminMapper.toSuspendUserRequest(id)));
    }

    @Operation(summary = "Reactivate a suspended user")
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN','OPS')")
    public UserProfile reactivate(@PathVariable UUID id) {
        return AdminMapper.mapReactivateUserResponse(
                reactivateUserProcessor.process(AdminMapper.toReactivateUserRequest(id)));
    }
}
