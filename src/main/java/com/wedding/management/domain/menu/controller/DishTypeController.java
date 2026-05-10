package com.wedding.management.domain.menu.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.menu.dto.DishTypeRequest;
import com.wedding.management.domain.menu.dto.DishTypeResponse;
import com.wedding.management.domain.menu.enums.DishTypeStatus;
import com.wedding.management.domain.menu.service.DishTypeService;
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
@RequestMapping("/api/v1/dish-types")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class DishTypeController {
    private final DishTypeService service;
    public DishTypeController(DishTypeService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ApiResponse<DishTypeResponse>> create(@Valid @RequestBody DishTypeRequest request, Principal principal) {
        DishTypeResponse response = service.createDishType(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<DishTypeResponse>builder().success(true).message("MSG48: Tạo thành công").data(response).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DishTypeResponse>> update(@PathVariable UUID id, @Valid @RequestBody DishTypeRequest request, @RequestParam long lastModifiedAt, Principal principal) {
        DishTypeResponse response = service.updateDishType(id, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<DishTypeResponse>builder().success(true).message("MSG17: Cập nhật thành công").data(response).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<DishTypeResponse>>> search(@RequestParam(required = false) String nameKeyword, @RequestParam(required = false) DishTypeStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<DishTypeResponse> list = service.searchDishTypes(nameKeyword, status);
        Page<DishTypeResponse> resultPage = toPage(list, page, Math.min(size, 20));
        return ResponseEntity.ok(ApiResponse.<Page<DishTypeResponse>>builder().success(true).message(list.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công").data(resultPage).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean deactivateIfInUse, Principal principal) {
        service.deleteDishType(id, principal.getName(), deactivateIfInUse);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("MSG20: Xóa thành công").build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DishTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<DishTypeResponse>>builder().success(true).message("Lấy danh sách thành công").data(service.getAllDishTypes()).build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DishTypeResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.<List<DishTypeResponse>>builder().success(true).message("Lấy danh sách đang hoạt động thành công").data(service.getActiveDishTypes()).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DishTypeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<DishTypeResponse>builder().success(true).message("Lấy thông tin thành công").data(service.getDishTypeById(id)).build());
    }

    private Page<DishTypeResponse> toPage(List<DishTypeResponse> list, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= list.size()) return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        int toIndex = Math.min(fromIndex + size, list.size());
        return new PageImpl<>(list.subList(fromIndex, toIndex), PageRequest.of(page, size), list.size());
    }
}
