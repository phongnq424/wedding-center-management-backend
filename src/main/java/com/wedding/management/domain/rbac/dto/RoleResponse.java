package com.wedding.management.domain.rbac.dto;

import com.wedding.management.domain.rbac.enums.RoleStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data @Builder
public class RoleResponse {
    private UUID id;
    private String name;
    private String description;
    private RoleStatus status;
    private Set<PermissionResponse> permissions;
    private Integer permissionCount;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
