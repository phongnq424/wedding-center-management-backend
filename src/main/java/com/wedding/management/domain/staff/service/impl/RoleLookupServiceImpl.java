package com.wedding.management.domain.staff.service.impl;

import com.wedding.management.domain.staff.dto.RoleOptionDTO;
import com.wedding.management.domain.staff.service.RoleLookupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoleLookupServiceImpl implements RoleLookupService {

    /**
     * Placeholder because RBAC Role entity/repository may already exist in your project.
     * Replace this list with RoleRepository.findAllAvailableRolesExceptDirector().
     */
    @Override
    public List<RoleOptionDTO> getAvailableRolesExceptDirector() {
        return List.of(
                RoleOptionDTO.builder()
                        .roleId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .roleName("OPERATIONS_MANAGER")
                        .build(),
                RoleOptionDTO.builder()
                        .roleId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .roleName("MENU_MANAGER")
                        .build(),
                RoleOptionDTO.builder()
                        .roleId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                        .roleName("STAFF")
                        .build()
        );
    }

    @Override
    public boolean isRoleAvailableForStaff(UUID roleId, String roleName) {
        if (roleId == null || roleName == null || roleName.isBlank()) {
            return false;
        }

        return !"DIRECTOR".equalsIgnoreCase(roleName.trim());
    }
}