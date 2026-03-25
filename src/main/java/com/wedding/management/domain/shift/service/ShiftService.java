package com.wedding.management.domain.shift.service;

import com.wedding.management.domain.shift.dto.ShiftRequest;
import com.wedding.management.domain.shift.dto.ShiftResponse;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ShiftService {
    ShiftResponse createShift(ShiftRequest request);
    ShiftResponse updateShift(UUID id, ShiftRequest request);
    List<ShiftResponse> searchShifts(String name, LocalTime fromTime, LocalTime toTime, String status);
    void deleteShift(UUID id);
}