package com.wedding.management.domain.rbac.service;

import com.wedding.management.domain.rbac.dto.RoleRequest;
import com.wedding.management.domain.rbac.dto.RoleResponse;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    // UC7: Add New Role
    RoleResponse createRole(RoleRequest request, String currentUserId);

    // UC8: Update Role
    RoleResponse updateRole(UUID roleId, RoleRequest request, String currentUserId, long lastModifiedAt);

    // UC9: Search Role
    List<RoleResponse> searchRoles(String nameKeyword, RoleStatus status);

    // UC10: Delete Role
    void deleteRole(UUID roleId, String currentUserId);

    // Helper methods
    List<RoleResponse> getAllRoles();
    RoleResponse getRoleById(UUID roleId);
}
