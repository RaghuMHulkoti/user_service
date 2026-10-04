package com.learning.user_service.application.command;

import com.learning.user_service.domain.model.RoleCode;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolesCommand(@NotEmpty Set<RoleCode> roles) {
}
