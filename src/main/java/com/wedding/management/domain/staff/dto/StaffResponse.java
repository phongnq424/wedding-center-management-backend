package com.wedding.management.domain.staff.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class StaffResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String role;
    private String status;
}