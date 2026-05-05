package com.wedding.management.domain.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {
    @NotBlank(message = "Tên quyền hạn không được để trống")
    private String name;

    private String description;

    private String code;

    private String module;
}
