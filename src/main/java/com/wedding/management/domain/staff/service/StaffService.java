package com.wedding.management.domain.staff.service;

import com.wedding.management.domain.staff.dto.StaffRequest;
import com.wedding.management.domain.staff.dto.StaffResponse;
import java.util.List;
import java.util.UUID;

public interface StaffService {
    StaffResponse addStaff(StaffRequest request);

    StaffResponse updateStaff(UUID id, StaffRequest request);

    List<StaffResponse> searchStaffs(String name, UUID role, String status);

    void deleteStaff(UUID id);
}