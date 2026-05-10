package com.wedding.management.domain.menu.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.menu.dto.BeverageTypeRequest;
import com.wedding.management.domain.menu.dto.BeverageTypeResponse;
import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import com.wedding.management.domain.menu.service.BeverageTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/beverage-types")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class BeverageTypeController {
    private final BeverageTypeService service;
    public BeverageTypeController(BeverageTypeService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ApiResponse<BeverageTypeResponse>> create(@Valid @RequestBody BeverageTypeRequest request, Principal principal) {
        BeverageTypeResponse response = service.createBeverageType(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<BeverageTypeResponse>builder().success(true).message("MSG48: Tạo thành công").data(response).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BeverageTypeResponse>> update(@PathVariable UUID id, @Valid @RequestBody BeverageTypeRequest request, @RequestParam long lastModifiedAt, Principal principal) {
        BeverageTypeResponse response = service.updateBeverageType(id, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<BeverageTypeResponse>builder().success(true).message("MSG17: Cập nhật thành công").data(response).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BeverageTypeResponse>>> search(@RequestParam(required = false) String nameKeyword, @RequestParam(required = false) BeverageTypeStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<BeverageTypeResponse> list = service.searchBeverageTypes(nameKeyword, status);
        Page<BeverageTypeResponse> resultPage = toPage(list, page, Math.min(size, 20));
        return ResponseEntity.ok(ApiResponse.<Page<BeverageTypeResponse>>builder().success(true).message(list.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công").data(resultPage).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean deactivateIfInUse, Principal principal) {
        service.deleteBeverageType(id, principal.getName(), deactivateIfInUse);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("MSG20: Xóa thành công").build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BeverageTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<BeverageTypeResponse>>builder().success(true).message("Lấy danh sách thành công").data(service.getAllBeverageTypes()).build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BeverageTypeResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.<List<BeverageTypeResponse>>builder().success(true).message("Lấy danh sách đang hoạt động thành công").data(service.getActiveBeverageTypes()).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BeverageTypeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<BeverageTypeResponse>builder().success(true).message("Lấy thông tin thành công").data(service.getBeverageTypeById(id)).build());
    }

    private Page<BeverageTypeResponse> toPage(List<BeverageTypeResponse> list, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= list.size()) return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        int toIndex = Math.min(fromIndex + size, list.size());
        return new PageImpl<>(list.subList(fromIndex, toIndex), PageRequest.of(page, size), list.size());
    }
}
