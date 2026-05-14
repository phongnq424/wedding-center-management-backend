package com.wedding.management.domain.staff.service.impl;

import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.rbac.repository.RoleRepository;
import com.wedding.management.domain.staff.dto.RoleOptionDTO;
import com.wedding.management.domain.staff.service.RoleLookupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoleLookupServiceImpl implements RoleLookupService {

    private final RoleRepository roleRepository;

    public RoleLookupServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<RoleOptionDTO> getAvailableRolesExceptDirector() {
        return roleRepository.findAllActive().stream()
                .filter(role -> role.getStatus() == RoleStatus.ACTIVE)
                .filter(role -> !"DIRECTOR".equalsIgnoreCase(role.getName()))
                .map(role -> RoleOptionDTO.builder()
                        .roleId(role.getId())
                        .roleName(role.getName())
                        .build())
                .toList();
    }

    @Override
    public boolean isRoleAvailableForStaff(UUID roleId, String ignoredRoleName) {
        if (roleId == null) {
            return false;
        }

        return roleRepository.findById(roleId)
                .filter(role -> !Boolean.TRUE.equals(role.getIsDeleted()))
                .filter(role -> role.getStatus() == RoleStatus.ACTIVE)
                .filter(role -> !"DIRECTOR".equalsIgnoreCase(role.getName()))
                .isPresent();
    }

    @Override
    public Role getAvailableRoleForStaff(UUID roleId) {
        if (roleId == null) {
            throw new BadRequestException("MSG2: Vai trò không được để trống");
        }

        return roleRepository.findById(roleId)
                .filter(role -> !Boolean.TRUE.equals(role.getIsDeleted()))
                .filter(role -> role.getStatus() == RoleStatus.ACTIVE)
                .filter(role -> !"DIRECTOR".equalsIgnoreCase(role.getName()))
                .orElseThrow(() -> new BadRequestException("MSG2: Vai trò không hợp lệ"));
    }
}