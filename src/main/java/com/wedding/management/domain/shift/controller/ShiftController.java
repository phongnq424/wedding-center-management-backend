package com.wedding.management.domain.shift.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.shift.dto.ShiftRequest;
import com.wedding.management.domain.shift.dto.ShiftResponse;
import com.wedding.management.domain.shift.enums.ShiftStatus;
import com.wedding.management.domain.shift.service.ShiftService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    /**
     * UC23: Add New Shift
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ShiftResponse>> createShift(
            @Valid @RequestBody ShiftRequest request,
            Principal principal) {
        ShiftResponse response = shiftService.createShift(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ShiftResponse>builder()
                        .success(true)
                        .message("MSG48: Ca được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC24: Update Shift
     */
    @PutMapping("/{shiftId}")
    public ResponseEntity<ApiResponse<ShiftResponse>> updateShift(
            @PathVariable UUID shiftId,
            @Valid @RequestBody ShiftRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal) {
        ShiftResponse response = shiftService.updateShift(shiftId, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<ShiftResponse>builder()
                .success(true)
                .message("MSG17: Ca được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC25: Search Shift
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ShiftResponse>>> searchShifts(
            @RequestParam(required = false) String shiftName,
            @RequestParam(required = false) LocalTime startTimeFrom,
            @RequestParam(required = false) LocalTime endTimeTo,
            @RequestParam(required = false) ShiftStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(size, 20);
        List<ShiftResponse> shifts = shiftService.searchShifts(shiftName, startTimeFrom, endTimeTo, status);

        int fromIndex = page * safeSize;
        List<ShiftResponse> paginatedShifts = fromIndex >= shifts.size()
                ? List.of()
                : shifts.subList(fromIndex, Math.min(fromIndex + safeSize, shifts.size()));

        Page<ShiftResponse> pageShifts = new PageImpl<>(paginatedShifts, PageRequest.of(page, safeSize), shifts.size());

        return ResponseEntity.ok(ApiResponse.<Page<ShiftResponse>>builder()
                .success(true)
                .message(shifts.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pageShifts)
                .build());
    }

    /**
     * UC26: Delete Shift
     */
    @DeleteMapping("/{shiftId}")
    public ResponseEntity<ApiResponse<Void>> deleteShift(
            @PathVariable UUID shiftId,
            @RequestParam(defaultValue = "false") boolean deactivateIfInUse,
            Principal principal) {
        shiftService.deleteShift(shiftId, principal.getName(), deactivateIfInUse);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG20: Ca được xóa thành công")
                .build());
    }

    /**
     * Get all shifts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getAllShifts() {
        List<ShiftResponse> shifts = shiftService.getAllShifts();
        return ResponseEntity.ok(ApiResponse.<List<ShiftResponse>>builder()
                .success(true)
                .message("Lấy danh sách ca thành công")
                .data(shifts)
                .build());
    }

    /**
     * Get active shifts for dropdown
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getActiveShifts() {
        List<ShiftResponse> shifts = shiftService.getActiveShifts();
        return ResponseEntity.ok(ApiResponse.<List<ShiftResponse>>builder()
                .success(true)
                .message("Lấy danh sách ca đang hoạt động thành công")
                .data(shifts)
                .build());
    }

    /**
     * Get shift by ID
     */
    @GetMapping("/{shiftId}")
    public ResponseEntity<ApiResponse<ShiftResponse>> getShiftById(@PathVariable UUID shiftId) {
        ShiftResponse shift = shiftService.getShiftById(shiftId);
        return ResponseEntity.ok(ApiResponse.<ShiftResponse>builder()
                .success(true)
                .message("Lấy thông tin ca thành công")
                .data(shift)
                .build());
    }
}
