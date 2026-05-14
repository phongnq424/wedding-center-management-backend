package com.wedding.management.domain.staff.service;

import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.staff.dto.RoleOptionDTO;

import java.util.List;
import java.util.UUID;

public interface RoleLookupService {
    List<RoleOptionDTO> getAvailableRolesExceptDirector();
    boolean isRoleAvailableForStaff(UUID roleId, String roleName);
    Role getAvailableRoleForStaff(UUID roleId);
}