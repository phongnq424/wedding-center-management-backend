package com.wedding.management.domain.rbac.dto;

import com.wedding.management.domain.rbac.enums.PermissionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class PermissionResponse {
    private UUID id;
    private String name;
    private String description;
    private String code;
    private String module;
    private PermissionStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
