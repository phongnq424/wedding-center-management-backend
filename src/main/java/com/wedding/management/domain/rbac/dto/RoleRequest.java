package com.wedding.management.domain.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class RoleRequest {
    @NotBlank(message = "Tên vai trò không được để trống")
    private String name;

    private String description;

    private List<UUID> permissionIds;
}
