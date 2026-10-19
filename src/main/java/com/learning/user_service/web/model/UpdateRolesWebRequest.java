package com.learning.user_service.web.model;

import com.learning.user_service.core.domain.model.RoleCode;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolesWebRequest(@NotEmpty Set<RoleCode> roles) {
}
