package com.wedding.management.domain.rbac.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.rbac.dto.RoleRequest;
import com.wedding.management.domain.rbac.dto.RoleResponse;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import com.wedding.management.domain.rbac.service.RoleService;
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
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasRole('DIRECTOR')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * UC7: Add New Role
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest request,
            Principal principal) {
        RoleResponse response = roleService.createRole(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RoleResponse>builder()
                        .success(true)
                        .message("MSG48: Vai trò được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC8: Update Role
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody RoleRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal) {
        RoleResponse response = roleService.updateRole(roleId, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("MSG17: Vai trò được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC9: Search Role
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> searchRoles(
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) RoleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<RoleResponse> roles = roleService.searchRoles(nameKeyword, status);
        
        // Apply pagination
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, roles.size());
        List<RoleResponse> paginatedRoles = roles.subList(fromIndex, toIndex);
        
        Page<RoleResponse> pageRoles = new PageImpl<>(paginatedRoles, PageRequest.of(page, size), roles.size());
        
        return ResponseEntity.ok(ApiResponse.<Page<RoleResponse>>builder()
                .success(true)
                .message(roles.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pageRoles)
                .build());
    }

    /**
     * UC10: Delete Role
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable UUID roleId,
            Principal principal) {
        roleService.deleteRole(roleId, principal.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG20: Vai trò được xóa thành công")
                .build());
    }

    /**
     * Get all roles
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.<List<RoleResponse>>builder()
                .success(true)
                .message("Lấy danh sách vai trò thành công")
                .data(roles)
                .build());
    }

    /**
     * Get role by ID
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable UUID roleId) {
        RoleResponse role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Lấy thông tin vai trò thành công")
                .data(role)
                .build());
    }
}
