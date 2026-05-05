package com.wedding.management.domain.rbac.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.rbac.dto.PermissionRequest;
import com.wedding.management.domain.rbac.dto.PermissionResponse;
import com.wedding.management.domain.rbac.enums.PermissionStatus;
import com.wedding.management.domain.rbac.model.Permission;
import com.wedding.management.domain.rbac.repository.PermissionRepository;
import com.wedding.management.domain.rbac.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository, AuditLogRepository auditLogRepository) {
        this.permissionRepository = permissionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public PermissionResponse createPermission(PermissionRequest request, String currentUserId) {
        // BR-CRO-2: Validate permission name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên quyền hạn không được để trống");
        }

        // BR-CRO-3: Check uniqueness
        if (request.getCode() != null && !request.getCode().isBlank()) {
            Optional<Permission> existingPerm = permissionRepository.findByCode(request.getCode());
            if (existingPerm.isPresent() && !existingPerm.get().getIsDeleted()) {
                throw new BadRequestException("MSG49: Mã quyền hạn đã tồn tại");
            }
        }

        // BR-CRO-4: Create permission
        Permission permission = Permission.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .module(request.getModule())
                .status(PermissionStatus.ACTIVE)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Permission savedPermission = permissionRepository.save(permission);

        // Log audit
        saveAuditLog(currentUserId, "CREATE_PERMISSION", savedPermission.getId(), savedPermission.getName());

        return mapToPermissionResponse(savedPermission);
    }

    @Override
    public PermissionResponse updatePermission(UUID permissionId, PermissionRequest request, String currentUserId, long lastModifiedAt) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn không tồn tại"));

        if (permission.getIsDeleted()) {
            throw new ResourceNotFoundException("Quyền hạn đã bị xóa");
        }

        // BR-UPE-2: Validate permission name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên quyền hạn không được để trống");
        }

        // BR-UPE-3: Check uniqueness (excluding current record)
        if (request.getCode() != null && !request.getCode().isBlank() && !permission.getCode().equals(request.getCode())) {
            Optional<Permission> existingPerm = permissionRepository.findByCode(request.getCode());
            if (existingPerm.isPresent() && !existingPerm.get().getIsDeleted()) {
                throw new BadRequestException("MSG49: Mã quyền hạn đã tồn tại");
            }
        }

        // BR-UPE-3: Optimistic locking - check version conflict
        if (permission.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        // BR-UPE-4: Update permission
        permission.setName(request.getName());
        permission.setCode(request.getCode());
        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());
        permission.setUpdatedBy(currentUserId);
        permission.setUpdatedAt(Instant.now());

        Permission updatedPermission = permissionRepository.save(permission);

        // Log audit
        saveAuditLog(currentUserId, "UPDATE_PERMISSION", updatedPermission.getId(), updatedPermission.getName());

        return mapToPermissionResponse(updatedPermission);
    }

    @Override
    public List<PermissionResponse> searchPermissions(String nameKeyword, PermissionStatus status) {
        List<Permission> permissions;

        // BR-SRO-03: Search with filters
        if (nameKeyword != null && !nameKeyword.isBlank() && status != null) {
            permissions = permissionRepository.searchByNameAndStatus(nameKeyword, status);
        } else if (nameKeyword != null && !nameKeyword.isBlank()) {
            permissions = permissionRepository.searchByName(nameKeyword);
        } else if (status != null) {
            permissions = permissionRepository.findByStatus(status);
        } else {
            // BR-SRO-01: Default - retrieve all permissions sorted by last modified DESC
            permissions = permissionRepository.findAllActive();
        }

        // BR-SRO-04: Map to response
        return permissions.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePermission(UUID permissionId, String currentUserId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn không tồn tại"));

        if (permission.getIsDeleted()) {
            throw new ResourceNotFoundException("Quyền hạn đã bị xóa");
        }

        // BR-DPE-3: Check if permission is assigned to roles
        long roleCount = permissionRepository.countActiveRoleByPermission(permissionId);

        if (roleCount == 0) {
            // Case 1: Can soft delete
            permission.setIsDeleted(true);
            permission.setUpdatedBy(currentUserId);
            permission.setUpdatedAt(Instant.now());
            permissionRepository.save(permission);

            // Log audit
            saveAuditLog(currentUserId, "DELETE_PERMISSION", permissionId, permission.getName());
        } else {
            // Case 2: Permission is in use - offer deactivation
            // MSG: "This permission is currently assigned to role. You cannot delete it. Do you want to deactivate this permission instead?"
            // If user confirms deactivation
            permission.setStatus(PermissionStatus.INACTIVE);
            permission.setUpdatedBy(currentUserId);
            permission.setUpdatedAt(Instant.now());
            permissionRepository.save(permission);

            // Log audit
            saveAuditLog(currentUserId, "DEACTIVATE_PERMISSION", permissionId, permission.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAllActive().stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Quyền hạn không tồn tại"));

        if (permission.getIsDeleted()) {
            throw new ResourceNotFoundException("Quyền hạn đã bị xóa");
        }

        return mapToPermissionResponse(permission);
    }

    private PermissionResponse mapToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .code(permission.getCode())
                .description(permission.getDescription())
                .module(permission.getModule())
                .status(permission.getStatus())
                .lastModifiedAt(permission.getUpdatedAt())
                .lastModifiedBy(permission.getUpdatedBy())
                .build();
    }

    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) {
        try {
            UUID userUUID = UUID.fromString(userId);
            AuditLog auditLog = AuditLog.builder()
                    .userId(userUUID)
                    .action(action)
                    .targetId(targetId)
                    .targetName(targetName)
                    .createdAt(Instant.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (IllegalArgumentException e) {
            // If userId is not a valid UUID, skip audit logging
        }
    }
}
