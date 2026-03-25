package com.wedding.management.domain.iam.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class RoleRequest {
    private String name;
    private String description;
    private List<UUID> permissionIds;
}