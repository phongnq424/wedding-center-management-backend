package com.wedding.management.domain.service.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.service.dto.ServiceRequest;
import com.wedding.management.domain.service.dto.ServiceResponse;
import com.wedding.management.domain.service.enums.ServiceStatus;
import com.wedding.management.domain.service.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * UC27: Add New Service
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody ServiceRequest request,
            Principal principal) {
        ServiceResponse response = serviceService.createService(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ServiceResponse>builder()
                        .success(true)
                        .message("MSG48: Dịch vụ được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC28: Update Service
     */
    @PutMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable UUID serviceId,
            @Valid @RequestBody ServiceRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal) {
        ServiceResponse response = serviceService.updateService(serviceId, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<ServiceResponse>builder()
                .success(true)
                .message("MSG17: Dịch vụ được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC29: Search Service
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ServiceResponse>>> searchServices(
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) ServiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(size, 20);
        List<ServiceResponse> services = serviceService.searchServices(nameKeyword, minPrice, status);

        int fromIndex = page * safeSize;
        List<ServiceResponse> paginatedServices = fromIndex >= services.size()
                ? List.of()
                : services.subList(fromIndex, Math.min(fromIndex + safeSize, services.size()));

        Page<ServiceResponse> pageServices = new PageImpl<>(paginatedServices, PageRequest.of(page, safeSize), services.size());

        return ResponseEntity.ok(ApiResponse.<Page<ServiceResponse>>builder()
                .success(true)
                .message(services.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pageServices)
                .build());
    }

    /**
     * UC30: Delete Service
     */
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @PathVariable UUID serviceId,
            @RequestParam(defaultValue = "false") boolean deactivateIfInUse,
            Principal principal) {
        serviceService.deleteService(serviceId, principal.getName(), deactivateIfInUse);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG20: Dịch vụ được xóa thành công")
                .build());
    }

    /**
     * Get all services
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices() {
        List<ServiceResponse> services = serviceService.getAllServices();
        return ResponseEntity.ok(ApiResponse.<List<ServiceResponse>>builder()
                .success(true)
                .message("Lấy danh sách dịch vụ thành công")
                .data(services)
                .build());
    }

    /**
     * Get active services for dropdown
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getActiveServices() {
        List<ServiceResponse> services = serviceService.getActiveServices();
        return ResponseEntity.ok(ApiResponse.<List<ServiceResponse>>builder()
                .success(true)
                .message("Lấy danh sách dịch vụ đang hoạt động thành công")
                .data(services)
                .build());
    }

    /**
     * Get service by ID
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceById(@PathVariable UUID serviceId) {
        ServiceResponse service = serviceService.getServiceById(serviceId);
        return ResponseEntity.ok(ApiResponse.<ServiceResponse>builder()
                .success(true)
                .message("Lấy thông tin dịch vụ thành công")
                .data(service)
                .build());
    }
}
