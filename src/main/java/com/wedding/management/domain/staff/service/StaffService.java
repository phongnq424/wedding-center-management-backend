package com.wedding.management.domain.staff.service;

import com.wedding.management.domain.staff.dto.RoleOptionDTO;
import com.wedding.management.domain.staff.dto.StaffRequest;
import com.wedding.management.domain.staff.dto.StaffResponse;
import com.wedding.management.domain.staff.enums.StaffStatus;

import java.util.List;
import java.util.UUID;

public interface StaffService {

    // UC31: Add New Staff
    StaffResponse createStaff(StaffRequest request, String currentUserId);

    // UC32: Update Staff
    StaffResponse updateStaff(UUID staffId, StaffRequest request, String currentUserId, long lastModifiedAt);

    // UC33: Search Staff
    List<StaffResponse> searchStaff(
            String fullName,
            String email,
            String phoneNumber,
            UUID roleId,
            StaffStatus status
    );

    // UC34: Delete Staff
    void deleteStaff(UUID staffId, String currentPassword, String currentUserId);

    // Helper methods
    List<StaffResponse> getAllStaff();

    StaffResponse getStaffById(UUID staffId);

    List<RoleOptionDTO> getAvailableRoles();
}