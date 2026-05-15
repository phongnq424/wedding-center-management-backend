package com.wedding.management.domain.menu.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.menu.dto.BeverageRequest;
import com.wedding.management.domain.menu.dto.BeverageResponse;
import com.wedding.management.domain.menu.enums.BeverageStatus;
import com.wedding.management.domain.menu.service.BeverageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/beverages")
@PreAuthorize("hasAuthority('MENU_FULL_ACCESS')")
public class BeverageController {
    private final BeverageService beverageService;
    public BeverageController(BeverageService beverageService) { this.beverageService = beverageService; }

    @PostMapping
    public ResponseEntity<ApiResponse<BeverageResponse>> createBeverage(@Valid @RequestBody BeverageRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<BeverageResponse>builder().success(true).message("MSG48: Thức uống được tạo thành công").data(beverageService.createBeverage(request, principal.getName())).build());
    }

    @PutMapping("/{beverageId}")
    public ResponseEntity<ApiResponse<BeverageResponse>> updateBeverage(@PathVariable UUID beverageId, @Valid @RequestBody BeverageRequest request, @RequestParam long lastModifiedAt, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<BeverageResponse>builder().success(true).message("MSG17: Thức uống được cập nhật thành công").data(beverageService.updateBeverage(beverageId, request, principal.getName(), lastModifiedAt)).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BeverageResponse>>> searchBeverages(@RequestParam(required = false) String beverageName, @RequestParam(required = false) UUID beverageTypeId, @RequestParam(required = false) Double priceFrom, @RequestParam(required = false) Double priceTo, @RequestParam(required = false) BeverageStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<BeverageResponse> list = beverageService.searchBeverages(beverageName, beverageTypeId, priceFrom, priceTo, status);
        Page<BeverageResponse> resultPage = toPage(list, page, Math.min(size, 20));
        return ResponseEntity.ok(ApiResponse.<Page<BeverageResponse>>builder().success(true).message(list.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công").data(resultPage).build());
    }

    @DeleteMapping("/{beverageId}")
    public ResponseEntity<ApiResponse<Void>> deleteBeverage(@PathVariable UUID beverageId, @RequestParam(defaultValue = "false") boolean deactivateIfInUse, Principal principal) {
        beverageService.deleteBeverage(beverageId, principal.getName(), deactivateIfInUse);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("MSG20: Thức uống được xóa thành công").build());
    }

    @GetMapping public ResponseEntity<ApiResponse<List<BeverageResponse>>> getAllBeverages() { return ResponseEntity.ok(ApiResponse.<List<BeverageResponse>>builder().success(true).message("Lấy danh sách thức uống thành công").data(beverageService.getAllBeverages()).build()); }
    @GetMapping("/active") public ResponseEntity<ApiResponse<List<BeverageResponse>>> getActiveBeverages() { return ResponseEntity.ok(ApiResponse.<List<BeverageResponse>>builder().success(true).message("Lấy danh sách thức uống đang hoạt động thành công").data(beverageService.getActiveBeverages()).build()); }
    @GetMapping("/{beverageId}") public ResponseEntity<ApiResponse<BeverageResponse>> getBeverageById(@PathVariable UUID beverageId) { return ResponseEntity.ok(ApiResponse.<BeverageResponse>builder().success(true).message("Lấy thông tin thức uống thành công").data(beverageService.getBeverageById(beverageId)).build()); }

    private Page<BeverageResponse> toPage(List<BeverageResponse> list, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= list.size()) return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        return new PageImpl<>(list.subList(fromIndex, Math.min(fromIndex + size, list.size())), PageRequest.of(page, size), list.size());
    }
}
