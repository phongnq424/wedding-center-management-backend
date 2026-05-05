package com.wedding.management.domain.staff.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.staff.dto.DeleteStaffRequest;
import com.wedding.management.domain.staff.dto.RoleOptionDTO;
import com.wedding.management.domain.staff.dto.StaffRequest;
import com.wedding.management.domain.staff.dto.StaffResponse;
import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    /**
     * UC31: Add New Staff
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request,
            Principal principal
    ) {
        StaffResponse response = staffService.createStaff(request, principal.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<StaffResponse>builder()
                        .success(true)
                        .message("MSG48: Nhân viên được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC32: Update Staff
     */
    @PutMapping("/{staffId}")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable UUID staffId,
            @Valid @RequestBody StaffRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal
    ) {
        StaffResponse response = staffService.updateStaff(
                staffId,
                request,
                principal.getName(),
                lastModifiedAt
        );

        return ResponseEntity.ok(ApiResponse.<StaffResponse>builder()
                .success(true)
                .message("MSG17: Nhân viên được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC33: Search Staff
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<StaffResponse>>> searchStaff(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) StaffStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int finalSize = Math.min(size, 20);

        List<StaffResponse> staffList = staffService.searchStaff(
                fullName,
                email,
                phoneNumber,
                roleId,
                status
        );

        int fromIndex = page * finalSize;
        List<StaffResponse> paginatedStaff;

        if (fromIndex >= staffList.size()) {
            paginatedStaff = List.of();
        } else {
            int toIndex = Math.min(fromIndex + finalSize, staffList.size());
            paginatedStaff = staffList.subList(fromIndex, toIndex);
        }

        Page<StaffResponse> pageStaff = new PageImpl<>(
                paginatedStaff,
                PageRequest.of(page, finalSize),
                staffList.size()
        );

        return ResponseEntity.ok(ApiResponse.<Page<StaffResponse>>builder()
                .success(true)
                .message(staffList.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pageStaff)
                .build());
    }

    /**
     * UC34: Delete Staff
     */
    @DeleteMapping("/{staffId}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable UUID staffId,
            @Valid @RequestBody DeleteStaffRequest request,
            Principal principal
    ) {
        staffService.deleteStaff(staffId, request.getCurrentPassword(), principal.getName());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG20: Nhân viên được xóa thành công")
                .build());
    }

    /**
     * Get all staff
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff() {
        List<StaffResponse> staffList = staffService.getAllStaff();

        return ResponseEntity.ok(ApiResponse.<List<StaffResponse>>builder()
                .success(true)
                .message("Lấy danh sách nhân viên thành công")
                .data(staffList)
                .build());
    }

    /**
     * Get staff by ID
     */
    @GetMapping("/{staffId}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable UUID staffId) {
        StaffResponse staff = staffService.getStaffById(staffId);

        return ResponseEntity.ok(ApiResponse.<StaffResponse>builder()
                .success(true)
                .message("Lấy thông tin nhân viên thành công")
                .data(staff)
                .build());
    }

    /**
     * BR-CST-1 / BR-UST-1: GetAllRoles except Director
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleOptionDTO>>> getAvailableRoles() {
        List<RoleOptionDTO> roles = staffService.getAvailableRoles();

        return ResponseEntity.ok(ApiResponse.<List<RoleOptionDTO>>builder()
                .success(true)
                .message("Lấy danh sách vai trò thành công")
                .data(roles)
                .build());
    }
}