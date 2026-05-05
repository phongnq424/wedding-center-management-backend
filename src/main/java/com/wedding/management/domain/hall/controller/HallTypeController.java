package com.wedding.management.domain.hall.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.hall.dto.HallTypeRequest;
import com.wedding.management.domain.hall.dto.HallTypeResponse;
import com.wedding.management.domain.hall.enums.HallTypeStatus;
import com.wedding.management.domain.hall.service.HallTypeService;
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
@RequestMapping("/api/v1/hall-types")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class HallTypeController {

    private final HallTypeService hallTypeService;

    public HallTypeController(HallTypeService hallTypeService) {
        this.hallTypeService = hallTypeService;
    }

    /**
     * UC15: Add New Hall Type
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HallTypeResponse>> createHallType(
            @Valid @RequestBody HallTypeRequest request,
            Principal principal) {
        HallTypeResponse response = hallTypeService.createHallType(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<HallTypeResponse>builder()
                        .success(true)
                        .message("Loại sảnh được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC16: Update Hall Type
     */
    @PutMapping("/{hallTypeId}")
    public ResponseEntity<ApiResponse<HallTypeResponse>> updateHallType(
            @PathVariable UUID hallTypeId,
            @Valid @RequestBody HallTypeRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal) {
        HallTypeResponse response = hallTypeService.updateHallType(hallTypeId, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<HallTypeResponse>builder()
                .success(true)
                .message("Loại sảnh được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC17: Search Hall Type
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<HallTypeResponse>>> searchHallTypes(
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) Double minBasePrice,
            @RequestParam(required = false) HallTypeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HallTypeResponse> hallTypes = hallTypeService.searchHallTypes(nameKeyword, minBasePrice, status);
        
        // Apply pagination
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, hallTypes.size());
        List<HallTypeResponse> paginatedHallTypes = hallTypes.subList(fromIndex, toIndex);
        
        Page<HallTypeResponse> pageHallTypes = new PageImpl<>(paginatedHallTypes, PageRequest.of(page, size), hallTypes.size());
        
        return ResponseEntity.ok(ApiResponse.<Page<HallTypeResponse>>builder()
                .success(true)
                .message(hallTypes.isEmpty() ? "Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pageHallTypes)
                .build());
    }

    /**
     * UC18: Delete Hall Type
     */
    @DeleteMapping("/{hallTypeId}")
    public ResponseEntity<ApiResponse<Void>> deleteHallType(
            @PathVariable UUID hallTypeId,
            Principal principal) {
        hallTypeService.deleteHallType(hallTypeId, principal.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Loại sảnh được xóa thành công")
                .build());
    }

    /**
     * Get all hall types
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HallTypeResponse>>> getAllHallTypes() {
        List<HallTypeResponse> hallTypes = hallTypeService.getAllHallTypes();
        return ResponseEntity.ok(ApiResponse.<List<HallTypeResponse>>builder()
                .success(true)
                .message("Lấy danh sách loại sảnh thành công")
                .data(hallTypes)
                .build());
    }

    /**
     * Get hall type by ID
     */
    @GetMapping("/{hallTypeId}")
    public ResponseEntity<ApiResponse<HallTypeResponse>> getHallTypeById(@PathVariable UUID hallTypeId) {
        HallTypeResponse hallType = hallTypeService.getHallTypeById(hallTypeId);
        return ResponseEntity.ok(ApiResponse.<HallTypeResponse>builder()
                .success(true)
                .message("Lấy thông tin loại sảnh thành công")
                .data(hallType)
                .build());
    }
}
