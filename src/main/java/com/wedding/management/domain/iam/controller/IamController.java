package com.wedding.management.domain.iam.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.iam.dto.*;
import com.wedding.management.domain.iam.model.Permission;
import com.wedding.management.domain.iam.service.IamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/iam")
@RequiredArgsConstructor
public class IamController {

    private final IamService iamService;

    @GetMapping("/permissions")
    public ApiResponse<List<Permission>> getPermissions() {
        return ApiResponse.<List<Permission>>builder()
                .status(200)
                .data(iamService.getAllPermissions())
                .build();
    }

    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .status(201)
                .data(iamService.createRole(request))
                .build();
    }

    @PatchMapping("/roles/{id}/permissions")
    public ApiResponse<RoleResponse> assignPermissions(
            @PathVariable UUID id,
            @RequestBody List<UUID> permissionIds) {
        return ApiResponse.<RoleResponse>builder()
                .status(200)
                .message("Cập nhật quyền cho vai trò thành công")
                .data(iamService.updateRolePermissions(id, permissionIds))
                .build();
    }
}