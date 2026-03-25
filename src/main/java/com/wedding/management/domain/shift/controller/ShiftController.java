package com.wedding.management.domain.shift.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.shift.dto.ShiftRequest;
import com.wedding.management.domain.shift.dto.ShiftResponse;
import com.wedding.management.domain.shift.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShiftResponse> create(@Valid @RequestBody ShiftRequest request) {
        return ApiResponse.<ShiftResponse>builder()
                .status(201)
                .message("Tạo ca làm việc mới thành công")
                .data(shiftService.createShift(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ShiftResponse> update(@PathVariable UUID id, @Valid @RequestBody ShiftRequest request) {
        return ApiResponse.<ShiftResponse>builder()
                .status(200)
                .message("Cập nhật ca thành công")
                .data(shiftService.updateShift(id, request))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<ShiftResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime fromTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime toTime,
            @RequestParam(required = false) String status) {
        return ApiResponse.<List<ShiftResponse>>builder()
                .status(200)
                .message("Lấy danh sách ca thành công")
                .data(shiftService.searchShifts(name, fromTime, toTime, status))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        shiftService.deleteShift(id);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Đã xóa ca thành công (soft delete)")
                .build();
    }
}