package com.wedding.management.domain.menu.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.menu.dto.DishRequest;
import com.wedding.management.domain.menu.dto.DishResponse;
import com.wedding.management.domain.menu.enums.DishStatus;
import com.wedding.management.domain.menu.service.DishService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/dishes")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DishResponse>> createDish(
            @RequestParam String name,
            @RequestParam UUID dishTypeId,
            @RequestParam Double unitPrice,
            @RequestParam(required = false) MultipartFile dishImage,
            @RequestParam(required = false) String description,
            Principal principal) {
        DishRequest request = new DishRequest();
        request.setName(name);
        request.setDishTypeId(dishTypeId);
        request.setUnitPrice(unitPrice);
        request.setDishImage(dishImage);
        request.setDescription(description);
        request.setStatus(DishStatus.ACTIVE);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DishResponse>builder().success(true).message("MSG48: Món ăn được tạo thành công")
                        .data(dishService.createDish(request, principal.getName())).build());
    }

    @PutMapping("/{dishId}")
    public ResponseEntity<ApiResponse<DishResponse>> updateDish(
            @PathVariable UUID dishId,
            @RequestParam String name,
            @RequestParam UUID dishTypeId,
            @RequestParam Double unitPrice,
            @RequestParam(required = false) MultipartFile dishImage,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) DishStatus status,
            @RequestParam long lastModifiedAt,
            Principal principal) {
        DishRequest request = new DishRequest();
        request.setName(name);
        request.setDishTypeId(dishTypeId);
        request.setUnitPrice(unitPrice);
        request.setDishImage(dishImage);
        request.setDescription(description);
        request.setStatus(status);

        return ResponseEntity
                .ok(ApiResponse.<DishResponse>builder().success(true).message("MSG17: Món ăn được cập nhật thành công")
                        .data(dishService.updateDish(dishId, request, principal.getName(), lastModifiedAt)).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<DishResponse>>> searchDishes(@RequestParam(required = false) String dishName,
            @RequestParam(required = false) UUID dishTypeId, @RequestParam(required = false) Double priceFrom,
            @RequestParam(required = false) Double priceTo, @RequestParam(required = false) DishStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<DishResponse> list = dishService.searchDishes(dishName, dishTypeId, priceFrom, priceTo, status);
        Page<DishResponse> resultPage = toPage(list, page, Math.min(size, 20));
        return ResponseEntity.ok(ApiResponse.<Page<DishResponse>>builder().success(true)
                .message(list.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công").data(resultPage)
                .build());
    }

    @DeleteMapping("/{dishId}")
    public ResponseEntity<ApiResponse<Void>> deleteDish(@PathVariable UUID dishId,
            @RequestParam(defaultValue = "false") boolean deactivateIfInUse, Principal principal) {
        dishService.deleteDish(dishId, principal.getName(), deactivateIfInUse);
        return ResponseEntity
                .ok(ApiResponse.<Void>builder().success(true).message("MSG20: Món ăn được xóa thành công").build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DishResponse>>> getAllDishes() {
        return ResponseEntity.ok(ApiResponse.<List<DishResponse>>builder().success(true)
                .message("Lấy danh sách món ăn thành công").data(dishService.getAllDishes()).build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DishResponse>>> getActiveDishes() {
        return ResponseEntity.ok(ApiResponse.<List<DishResponse>>builder().success(true)
                .message("Lấy danh sách món ăn đang hoạt động thành công").data(dishService.getActiveDishes()).build());
    }

    @GetMapping("/active/by-type/{dishTypeId}")
    public ResponseEntity<ApiResponse<List<DishResponse>>> getActiveDishesByType(@PathVariable UUID dishTypeId) {
        return ResponseEntity.ok(ApiResponse.<List<DishResponse>>builder().success(true)
                .message("Lấy danh sách món ăn theo loại thành công")
                .data(dishService.getActiveDishesByType(dishTypeId)).build());
    }

    @GetMapping("/{dishId}")
    public ResponseEntity<ApiResponse<DishResponse>> getDishById(@PathVariable UUID dishId) {
        return ResponseEntity.ok(ApiResponse.<DishResponse>builder().success(true)
                .message("Lấy thông tin món ăn thành công").data(dishService.getDishById(dishId)).build());
    }

    private Page<DishResponse> toPage(List<DishResponse> list, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= list.size())
            return new PageImpl<>(List.of(), PageRequest.of(page, size), list.size());
        return new PageImpl<>(list.subList(fromIndex, Math.min(fromIndex + size, list.size())),
                PageRequest.of(page, size), list.size());
    }
}
