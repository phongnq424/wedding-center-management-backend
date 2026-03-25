package com.wedding.management.domain.staff.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.staff.dto.StaffRequest;
import com.wedding.management.domain.staff.dto.StaffResponse;
import com.wedding.management.domain.staff.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ApiResponse<StaffResponse> addStaff(@Valid @RequestBody StaffRequest request) {
        return ApiResponse.<StaffResponse>builder()
                .status(201)
                .message("Thêm nhân viên mới thành công. Mật khẩu tạm đã được gửi.")
                .data(staffService.addStaff(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StaffResponse> updateStaff(@PathVariable UUID id, @Valid @RequestBody StaffRequest request) {
        return ApiResponse.<StaffResponse>builder()
                .status(200)
                .message("Cập nhật thông tin nhân viên thành công")
                .data(staffService.updateStaff(id, request))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<StaffResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) String status) {
        return ApiResponse.<List<StaffResponse>>builder()
                .status(200)
                .data(staffService.searchStaffs(name, roleId, status))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        staffService.deleteStaff(id);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Đã thu hồi quyền truy cập của nhân viên này")
                .build();
    }
}