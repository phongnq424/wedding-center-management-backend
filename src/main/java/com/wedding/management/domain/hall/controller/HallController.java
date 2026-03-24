package com.wedding.management.domain.hall.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.hall.dto.*;
import com.wedding.management.domain.hall.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HallResponse> createHall(@Valid @RequestBody HallRequest request) {
        return ApiResponse.<HallResponse>builder()
                .status(201)
                .message("Tạo sảnh mới thành công")
                .data(hallService.createHall(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<HallResponse> updateHall(
            @PathVariable Long id,
            @Valid @RequestBody HallRequest request) {
        return ApiResponse.<HallResponse>builder()
                .status(200)
                .message("Cập nhật thông tin sảnh thành công")
                .data(hallService.updateHall(id, request))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<HallResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String status) {
        return ApiResponse.<List<HallResponse>>builder()
                .status(200)
                .message("Tìm thấy " + name + " sảnh phù hợp") // Có thể tinh chỉnh câu thông báo
                .data(hallService.searchHalls(name, capacity, status))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Đã chuyển sảnh vào trạng thái lưu trữ (xóa mềm)")
                .build();
    }

    @PatchMapping("/{id}/activate")
    public ApiResponse<HallResponse> activateHall(@PathVariable Long id) {
        return ApiResponse.<HallResponse>builder()
                .status(200)
                .message("Sảnh đã được kích hoạt cho phép đặt tiệc")
                .data(hallService.activateHall(id))
                .build();
    }
}