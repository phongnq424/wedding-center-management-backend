package com.wedding.management.domain.iam.service;

import com.wedding.management.domain.iam.dto.*;
import com.wedding.management.domain.iam.model.Permission;
import java.util.List;
import java.util.UUID;

public interface IamService {
    RoleResponse createRole(RoleRequest request);
    RoleResponse updateRolePermissions(UUID roleId, List<UUID> permissionIds);
    List<Permission> getAllPermissions();
    List<RoleResponse> getAllRoles();
}