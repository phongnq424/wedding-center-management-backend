package com.wedding.management.domain.weddingpackage.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.weddingpackage.dto.WeddingPackageRequest;
import com.wedding.management.domain.weddingpackage.dto.WeddingPackageResponse;
import com.wedding.management.domain.weddingpackage.enums.WeddingPackageStatus;
import com.wedding.management.domain.weddingpackage.service.WeddingPackageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/wedding-packages")
@PreAuthorize("hasAuthority('PACKAGE_FULL_ACCESS')")
public class WeddingPackageController {
    private final WeddingPackageService weddingPackageService;

    public WeddingPackageController(WeddingPackageService weddingPackageService) {
        this.weddingPackageService = weddingPackageService;
    }

    /**
     * UC66: Add Wedding Package
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WeddingPackageResponse>> createWeddingPackage(
            @Valid @RequestBody WeddingPackageRequest request,
            Principal principal) {
        WeddingPackageResponse response = weddingPackageService.createWeddingPackage(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<WeddingPackageResponse>builder()
                        .success(true)
                        .message("MSG48: Gói tiệc được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC67: Update Wedding Package
     */
    @PutMapping("/{packageId}")
    public ResponseEntity<ApiResponse<WeddingPackageResponse>> updateWeddingPackage(
            @PathVariable UUID packageId,
            @Valid @RequestBody WeddingPackageRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal) {
        WeddingPackageResponse response = weddingPackageService.updateWeddingPackage(packageId, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<WeddingPackageResponse>builder()
                .success(true)
                .message("MSG17: Gói tiệc được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC68: Search Wedding Package
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<WeddingPackageResponse>>> searchWeddingPackages(
            @RequestParam(required = false) String packageName,
            @RequestParam(required = false) List<UUID> selectedDishComboIds,
            @RequestParam(required = false) List<UUID> selectedServiceIds,
            @RequestParam(required = false) List<UUID> selectedBeverageIds,
            @RequestParam(required = false) UUID hallTypeId,
            @RequestParam(required = false) UUID shiftId,
            @RequestParam(required = false) WeddingPackageStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<WeddingPackageResponse> packages = weddingPackageService.searchWeddingPackages(packageName, selectedDishComboIds, selectedServiceIds, selectedBeverageIds, hallTypeId, shiftId, status);
        Page<WeddingPackageResponse> resultPage = toPage(packages, page, Math.min(size, 20));
        return ResponseEntity.ok(ApiResponse.<Page<WeddingPackageResponse>>builder()
                .success(true)
                .message(packages.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(resultPage)
                .build());
    }

    /**
     * UC69: Delete Wedding Package
     */
    @DeleteMapping("/{packageId}")
    public ResponseEntity<ApiResponse<Void>> deleteWeddingPackage(
            @PathVariable UUID packageId,
            @RequestParam(defaultValue = "false") boolean deactivateIfInUse,
            Principal principal) {
        weddingPackageService.deleteWeddingPackage(packageId, principal.getName(), deactivateIfInUse);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG20: Gói tiệc được xóa thành công")
                .build());
    }

    /**
     * Get all wedding packages
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WeddingPackageResponse>>> getAllWeddingPackages() {
        List<WeddingPackageResponse> packages = weddingPackageService.getAllWeddingPackages();
        return ResponseEntity.ok(ApiResponse.<List<WeddingPackageResponse>>builder()
                .success(true)
                .message("Lấy danh sách gói tiệc thành công")
                .data(packages)
                .build());
    }

    /**
     * Get wedding package by ID
     */
    @GetMapping("/{packageId}")
    public ResponseEntity<ApiResponse<WeddingPackageResponse>> getWeddingPackageById(@PathVariable UUID packageId) {
        WeddingPackageResponse response = weddingPackageService.getWeddingPackageById(packageId);
        return ResponseEntity.ok(ApiResponse.<WeddingPackageResponse>builder()
                .success(true)
                .message("Lấy thông tin gói tiệc thành công")
                .data(response)
                .build());
    }

    private Page<WeddingPackageResponse> toPage(List<WeddingPackageResponse> list, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= list.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        }
        int toIndex = Math.min(fromIndex + size, list.size());
        return new PageImpl<>(list.subList(fromIndex, toIndex), PageRequest.of(page, size), list.size());
    }
}
