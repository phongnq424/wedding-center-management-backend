package com.wedding.management.domain.staff.dto;

import com.wedding.management.domain.staff.enums.StaffStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class StaffResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String description;
    private String staffImage;
    private UUID roleId;
    private String roleName;
    private StaffStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}