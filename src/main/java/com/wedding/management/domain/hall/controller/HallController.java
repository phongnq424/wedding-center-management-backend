package com.wedding.management.domain.hall.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.hall.dto.HallRequest;
import com.wedding.management.domain.hall.dto.HallResponse;
import com.wedding.management.domain.hall.enums.HallStatus;
import com.wedding.management.domain.hall.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/halls")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;
    private final ObjectMapper objectMapper;

    /**
     * UC19: Add New Hall
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<HallResponse>> createHall(
            @RequestPart("data") String data,
            @RequestPart(value = "hallImage", required = false) MultipartFile hallImage,
            Principal principal
    ) throws JsonProcessingException {

        HallRequest request = objectMapper.readValue(data, HallRequest.class);
        request.setHallImage(hallImage);
        HallResponse response = hallService.createHall(
                request,
                getCurrentUsername(principal)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<HallResponse>builder()
                        .success(true)
                        .message("MSG48: Sảnh được tạo thành công")
                        .data(response)
                        .build());
    }

    /**
     * UC20: Update Hall
     */
    @PutMapping(value = "/{hallId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<HallResponse>> updateHall(
            @PathVariable UUID hallId,
            @RequestPart("data") String data,
            @RequestPart(value = "hallImage", required = false) MultipartFile hallImage,
            @RequestParam long lastModifiedAt,
            Principal principal
    ) throws JsonProcessingException {

        HallRequest request = objectMapper.readValue(data, HallRequest.class);
        request.setHallImage(hallImage);
        HallResponse response = hallService.updateHall(
                hallId,
                request,
                getCurrentUsername(principal),
                lastModifiedAt
        );

        return ResponseEntity.ok(ApiResponse.<HallResponse>builder()
                .success(true)
                .message("MSG17: Sảnh được cập nhật thành công")
                .data(response)
                .build());
    }

    /**
     * UC21: Search Hall
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<HallResponse>>> searchHalls(
            @RequestParam(required = false) String hallName,
            @RequestParam(required = false) UUID hallTypeId,
            @RequestParam(required = false) Integer minTablesFrom,
            @RequestParam(required = false) Integer maxTablesTo,
            @RequestParam(required = false) HallStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<HallResponse> halls = hallService.searchHalls(
                hallName,
                hallTypeId,
                minTablesFrom,
                maxTablesTo,
                status
        );

        int fromIndex = page * size;

        if (fromIndex >= halls.size()) {
            Page<HallResponse> emptyPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(page, size),
                    halls.size()
            );

            return ResponseEntity.ok(ApiResponse.<Page<HallResponse>>builder()
                    .success(true)
                    .message("MSG12: Không tìm thấy kết quả")
                    .data(emptyPage)
                    .build());
        }

        int toIndex = Math.min(fromIndex + size, halls.size());
        List<HallResponse> paginatedHalls = halls.subList(fromIndex, toIndex);

        Page<HallResponse> pageHalls = new PageImpl<>(
                paginatedHalls,
                PageRequest.of(page, size),
                halls.size()
        );

        return ResponseEntity.ok(ApiResponse.<Page<HallResponse>>builder()
                .success(true)
                .message(halls.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pageHalls)
                .build());
    }

    /**
     * UC22: Delete Hall
     */
    @DeleteMapping("/{hallId}")
    public ResponseEntity<ApiResponse<Void>> deleteHall(
            @PathVariable UUID hallId,
            Principal principal
    ) {
        hallService.deleteHall(hallId, getCurrentUsername(principal));

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG20: Sảnh được xóa thành công")
                .build());
    }

    /**
     * Get all halls
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HallResponse>>> getAllHalls() {
        List<HallResponse> halls = hallService.getAllHalls();

        return ResponseEntity.ok(ApiResponse.<List<HallResponse>>builder()
                .success(true)
                .message("Lấy danh sách sảnh thành công")
                .data(halls)
                .build());
    }

    /**
     * Get hall by ID
     */
    @GetMapping("/{hallId}")
    public ResponseEntity<ApiResponse<HallResponse>> getHallById(
            @PathVariable UUID hallId
    ) {
        HallResponse hall = hallService.getHallById(hallId);

        return ResponseEntity.ok(ApiResponse.<HallResponse>builder()
                .success(true)
                .message("Lấy thông tin sảnh thành công")
                .data(hall)
                .build());
    }

    private String getCurrentUsername(Principal principal) {
        return principal != null ? principal.getName() : "SYSTEM";
    }
}