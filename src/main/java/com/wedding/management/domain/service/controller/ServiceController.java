package com.wedding.management.domain.service.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.service.dto.ServiceRequest;
import com.wedding.management.domain.service.dto.ServiceResponse;
import com.wedding.management.domain.service.service.WeddingServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final WeddingServiceService serviceService;

    @PostMapping
    public ApiResponse<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        return ApiResponse.<ServiceResponse>builder()
                .status(201)
                .message("Thêm dịch vụ thành công")
                .data(serviceService.createService(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ServiceResponse> update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) {
        return ApiResponse.<ServiceResponse>builder()
                .status(200)
                .message("Cập nhật dịch vụ thành công")
                .data(serviceService.updateService(id, request))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<ServiceResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ApiResponse.<List<ServiceResponse>>builder()
                .status(200)
                .message("Tra cứu dịch vụ thành công")
                .data(serviceService.searchServices(name, category, status))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        serviceService.deleteService(id);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Đã xóa mềm dịch vụ thành công")
                .build();
    }
}