package com.wedding.management.domain.shift.service;

import com.wedding.management.domain.shift.dto.ShiftRequest;
import com.wedding.management.domain.shift.dto.ShiftResponse;
import com.wedding.management.domain.shift.enums.ShiftStatus;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ShiftService {
    // UC23: Add New Shift
    ShiftResponse createShift(ShiftRequest request, String currentUserId);

    // UC24: Update Shift
    ShiftResponse updateShift(UUID shiftId, ShiftRequest request, String currentUserId, long lastModifiedAt);

    // UC25: Search Shift
    List<ShiftResponse> searchShifts(String shiftName, LocalTime startTimeFrom, LocalTime endTimeTo, ShiftStatus status);

    // UC26: Delete Shift
    void deleteShift(UUID shiftId, String currentUserId, boolean deactivateIfInUse);

    // Helper methods
    List<ShiftResponse> getAllShifts();
    List<ShiftResponse> getActiveShifts();
    ShiftResponse getShiftById(UUID shiftId);
}
