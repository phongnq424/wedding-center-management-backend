package com.wedding.management.domain.staff.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoleOptionDTO {
    private UUID roleId;
    private String roleName;
}