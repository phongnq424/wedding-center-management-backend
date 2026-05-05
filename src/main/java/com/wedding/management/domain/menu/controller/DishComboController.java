package com.wedding.management.domain.menu.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.menu.dto.DishComboRequest;
import com.wedding.management.domain.menu.dto.DishComboResponse;
import com.wedding.management.domain.menu.enums.DishComboStatus;
import com.wedding.management.domain.menu.service.DishComboService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/dish-combos")
@PreAuthorize("hasRole('MENU_MANAGER')")
public class DishComboController {
    private final DishComboService dishComboService;
    public DishComboController(DishComboService dishComboService) { this.dishComboService = dishComboService; }

    @PostMapping
    public ResponseEntity<ApiResponse<DishComboResponse>> createDishCombo(@Valid @RequestBody DishComboRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<DishComboResponse>builder().success(true).message("MSG48: Combo món ăn được tạo thành công").data(dishComboService.createDishCombo(request, principal.getName())).build());
    }

    @PutMapping("/{comboId}")
    public ResponseEntity<ApiResponse<DishComboResponse>> updateDishCombo(@PathVariable UUID comboId, @Valid @RequestBody DishComboRequest request, @RequestParam long lastModifiedAt, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<DishComboResponse>builder().success(true).message("MSG17: Combo món ăn được cập nhật thành công").data(dishComboService.updateDishCombo(comboId, request, principal.getName(), lastModifiedAt)).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<DishComboResponse>>> searchDishCombos(@RequestParam(required = false) String comboName, @RequestParam(required = false) UUID dishTypeId, @RequestParam(required = false) String dishName, @RequestParam(required = false) Double comboDiscountRateFrom, @RequestParam(required = false) Double comboDiscountRateTo, @RequestParam(required = false) Boolean isReplaceable, @RequestParam(required = false) DishComboStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<DishComboResponse> list = dishComboService.searchDishCombos(comboName, dishTypeId, dishName, comboDiscountRateFrom, comboDiscountRateTo, isReplaceable, status);
        Page<DishComboResponse> resultPage = toPage(list, page, Math.min(size, 20));
        return ResponseEntity.ok(ApiResponse.<Page<DishComboResponse>>builder().success(true).message(list.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công").data(resultPage).build());
    }

    @DeleteMapping("/{comboId}")
    public ResponseEntity<ApiResponse<Void>> deleteDishCombo(@PathVariable UUID comboId, @RequestParam(defaultValue = "false") boolean deactivateIfInUse, Principal principal) {
        dishComboService.deleteDishCombo(comboId, principal.getName(), deactivateIfInUse);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("MSG20: Combo món ăn được xóa thành công").build());
    }

    @GetMapping public ResponseEntity<ApiResponse<List<DishComboResponse>>> getAllDishCombos() { return ResponseEntity.ok(ApiResponse.<List<DishComboResponse>>builder().success(true).message("Lấy danh sách combo món ăn thành công").data(dishComboService.getAllDishCombos()).build()); }
    @GetMapping("/{comboId}") public ResponseEntity<ApiResponse<DishComboResponse>> getDishComboById(@PathVariable UUID comboId) { return ResponseEntity.ok(ApiResponse.<DishComboResponse>builder().success(true).message("Lấy thông tin combo món ăn thành công").data(dishComboService.getDishComboById(comboId)).build()); }

    private Page<DishComboResponse> toPage(List<DishComboResponse> list, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= list.size()) return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        return new PageImpl<>(list.subList(fromIndex, Math.min(fromIndex + size, list.size())), PageRequest.of(page, size), list.size());
    }
}
