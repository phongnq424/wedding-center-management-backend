package com.wedding.management.domain.rbac.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.rbac.dto.PermissionRequest;
import com.wedding.management.domain.rbac.dto.PermissionResponse;
import com.wedding.management.domain.rbac.enums.PermissionStatus;
import com.wedding.management.domain.rbac.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@PreAuthorize("hasRole('DIRECTOR')")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * UC11: Add New Permission
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request,
            Principal principal) {
        PermissionResponse response = permissionService.createPermission(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PermissionResponse>builder()
                        .success(true)
                        .message("MSG48: Quyền hạn được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC12: Update Permission
     */
    @PutMapping("/{permissionId}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable UUID permissionId,
            @Valid @RequestBody PermissionRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal) {
        PermissionResponse response = permissionService.updatePermission(permissionId, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("MSG17: Quyền hạn được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC13: Search Permission
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> searchPermissions(
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) PermissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<PermissionResponse> permissions = permissionService.searchPermissions(nameKeyword, status);
        
        // Apply pagination
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, permissions.size());
        List<PermissionResponse> paginatedPermissions = permissions.subList(fromIndex, toIndex);
        
        Page<PermissionResponse> pagePermissions = new PageImpl<>(paginatedPermissions, PageRequest.of(page, size), permissions.size());
        
        return ResponseEntity.ok(ApiResponse.<Page<PermissionResponse>>builder()
                .success(true)
                .message(permissions.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pagePermissions)
                .build());
    }

    /**
     * UC14: Delete Permission
     */
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(
            @PathVariable UUID permissionId,
            Principal principal) {
        permissionService.deletePermission(permissionId, principal.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG20: Quyền hạn được xóa thành công")
                .build());
    }

    /**
     * Get all permissions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Lấy danh sách quyền hạn thành công")
                .data(permissions)
                .build());
    }

    /**
     * Get permission by ID
     */
    @GetMapping("/{permissionId}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable UUID permissionId) {
        PermissionResponse permission = permissionService.getPermissionById(permissionId);
        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Lấy thông tin quyền hạn thành công")
                .data(permission)
                .build());
    }
}
