package com.wedding.management.domain.rbac.service;

import com.wedding.management.domain.rbac.dto.PermissionRequest;
import com.wedding.management.domain.rbac.dto.PermissionResponse;
import com.wedding.management.domain.rbac.enums.PermissionStatus;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
    // UC11: Add New Permission
    PermissionResponse createPermission(PermissionRequest request, String currentUserId);

    // UC12: Update Permission
    PermissionResponse updatePermission(UUID permissionId, PermissionRequest request, String currentUserId, long lastModifiedAt);

    // UC13: Search Permission
    List<PermissionResponse> searchPermissions(String nameKeyword, PermissionStatus status);

    // UC14: Delete Permission
    void deletePermission(UUID permissionId, String currentUserId);

    // Helper methods
    List<PermissionResponse> getAllPermissions();
    PermissionResponse getPermissionById(UUID permissionId);
}
