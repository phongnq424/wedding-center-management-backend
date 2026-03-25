package com.wedding.management.domain.iam.service.impl;

import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.iam.dto.*;
import com.wedding.management.domain.iam.model.*;
import com.wedding.management.domain.iam.repository.*;
import com.wedding.management.domain.iam.service.IamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IamServiceImpl implements IamService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();

        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse updateRolePermissions(UUID roleId, List<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tìm thấy"));

        // Chặn chỉnh sửa Role "DIRECTOR" nếu cần bảo mật cao
        if (role.getName().equals("DIRECTOR")) {
            throw new RuntimeException("Không thể sửa quyền của cấp bậc Giám đốc qua API");
        }

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        role.setPermissions(permissions);

        return mapToResponse(roleRepository.save(role));
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissionCodes(role.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()))
                .build();
    }
}