package com.wedding.management.domain.rbac.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.rbac.dto.PermissionResponse;
import com.wedding.management.domain.rbac.dto.RoleRequest;
import com.wedding.management.domain.rbac.dto.RoleResponse;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import com.wedding.management.domain.rbac.model.Permission;
import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.rbac.repository.PermissionRepository;
import com.wedding.management.domain.rbac.repository.RoleRepository;
import com.wedding.management.domain.rbac.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository, AuditLogRepository auditLogRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public RoleResponse createRole(RoleRequest request, String currentUserId) {
        // BR-CRO-2: Validate role name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên vai trò không được để trống");
        }

        // BR-CRO-3: Check uniqueness
        Optional<Role> existingRole = roleRepository.findByName(request.getName());
        if (existingRole.isPresent() && !existingRole.get().getIsDeleted()) {
            throw new BadRequestException("MSG49: Tên vai trò đã tồn tại");
        }

        // Load permissions if provided
        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            permissions = request.getPermissionIds().stream()
                    .map(permId -> permissionRepository.findById(permId)
                            .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn không tồn tại: " + permId)))
                    .collect(Collectors.toSet());
        }

        // BR-CRO-4: Create role
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(RoleStatus.ACTIVE)
                .permissions(permissions)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Role savedRole = roleRepository.save(role);

        // Log audit
        saveAuditLog(currentUserId, "CREATE_ROLE", savedRole.getId(), savedRole.getName());

        return mapToRoleResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(UUID roleId, RoleRequest request, String currentUserId, long lastModifiedAt) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại"));

        if (role.getIsDeleted()) {
            throw new ResourceNotFoundException("Vai trò đã bị xóa");
        }

        // BR-URO-2: Validate role name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên vai trò không được để trống");
        }

        // BR-URO-3: Check uniqueness (excluding current record)
        if (!role.getName().equals(request.getName())) {
            Optional<Role> existingRole = roleRepository.findByName(request.getName());
            if (existingRole.isPresent() && !existingRole.get().getIsDeleted()) {
                throw new BadRequestException("MSG49: Tên vai trò đã tồn tại");
            }
        }

        // BR-URO-3: Optimistic locking - check version conflict
        if (role.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        // Update permissions if provided
        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = request.getPermissionIds().stream()
                    .map(permId -> permissionRepository.findById(permId)
                            .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn không tồn tại: " + permId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        // BR-URO-4: Update role
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setUpdatedBy(currentUserId);
        role.setUpdatedAt(Instant.now());

        Role updatedRole = roleRepository.save(role);

        // Log audit
        saveAuditLog(currentUserId, "UPDATE_ROLE", updatedRole.getId(), updatedRole.getName());

        return mapToRoleResponse(updatedRole);
    }

    @Override
    public List<RoleResponse> searchRoles(String nameKeyword, RoleStatus status) {
        List<Role> roles;

        // BR-SRO-03: Search with filters
        if (nameKeyword != null && !nameKeyword.isBlank() && status != null) {
            roles = roleRepository.searchByNameAndStatus(nameKeyword, status);
        } else if (nameKeyword != null && !nameKeyword.isBlank()) {
            roles = roleRepository.searchByName(nameKeyword);
        } else if (status != null) {
            roles = roleRepository.findByStatus(status);
        } else {
            // BR-SRO-01: Default - retrieve all roles sorted by last modified DESC
            roles = roleRepository.findAllActive();
        }

        // BR-SRO-04: Map to response
        return roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRole(UUID roleId, String currentUserId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại"));

        if (role.getIsDeleted()) {
            throw new ResourceNotFoundException("Vai trò đã bị xóa");
        }

        // BR-DR-3: Check if role is assigned to staff
        long staffCount = roleRepository.countActiveStaffByRole(roleId);

        if (staffCount == 0) {
            // Case 1: Can soft delete
            role.setIsDeleted(true);
            role.setUpdatedBy(currentUserId);
            role.setUpdatedAt(Instant.now());
            roleRepository.save(role);

            // Log audit
            saveAuditLog(currentUserId, "DELETE_ROLE", roleId, role.getName());
        } else {
            // Case 2: Role is in use - offer deactivation
            // This returns a message for the UI to prompt user
            // MSG: "This role is currently assigned to staff. You cannot delete it. Do you want to deactivate this role instead?"
            // If user confirms deactivation
            role.setStatus(RoleStatus.INACTIVE);
            role.setUpdatedBy(currentUserId);
            role.setUpdatedAt(Instant.now());
            roleRepository.save(role);

            // Log audit
            saveAuditLog(currentUserId, "DEACTIVATE_ROLE", roleId, role.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAllActive().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò không tồn tại"));

        if (role.getIsDeleted()) {
            throw new ResourceNotFoundException("Vai trò đã bị xóa");
        }

        return mapToRoleResponse(role);
    }

    private RoleResponse mapToRoleResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                .map(perm -> PermissionResponse.builder()
                        .id(perm.getId())
                        .name(perm.getName())
                        .code(perm.getCode())
                        .module(perm.getModule())
                        .status(perm.getStatus())
                        .build())
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .status(role.getStatus())
                .permissions(permissionResponses)
                .permissionCount(permissionResponses.size())
                .lastModifiedAt(role.getUpdatedAt())
                .lastModifiedBy(role.getUpdatedBy())
                .build();
    }

    private void saveAuditLog(UUID userId, String action, UUID targetId, String targetName) {
        AuditLog auditLog = AuditLog.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000000")) // Placeholder - get from security context
                .action(action)
                .targetId(targetId)
                .targetName(targetName)
                .createdAt(Instant.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) {
        try {
            UUID userUUID = UUID.fromString(userId);
            saveAuditLog(userUUID, action, targetId, targetName);
        } catch (IllegalArgumentException e) {
            // If userId is not a valid UUID, skip audit logging or handle differently
        }
    }
}
