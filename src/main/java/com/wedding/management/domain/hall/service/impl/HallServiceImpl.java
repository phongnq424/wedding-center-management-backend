package com.wedding.management.domain.hall.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.config.supabase.SupabaseFileUploadService;
import com.wedding.management.domain.hall.dto.HallPricingDTO;
import com.wedding.management.domain.hall.dto.HallRequest;
import com.wedding.management.domain.hall.dto.HallResponse;
import com.wedding.management.domain.hall.enums.HallStatus;
import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.hall.model.HallPricing;
import com.wedding.management.domain.hall.model.HallType;
import com.wedding.management.domain.hall.repository.HallPricingRepository;
import com.wedding.management.domain.hall.repository.HallRepository;
import com.wedding.management.domain.hall.repository.HallTypeRepository;
import com.wedding.management.domain.hall.service.HallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final HallTypeRepository hallTypeRepository;
    private final HallPricingRepository hallPricingRepository;
    private final AuditLogRepository auditLogRepository;
    private final SupabaseFileUploadService fileUploadService;

    public HallServiceImpl(
            HallRepository hallRepository,
            HallTypeRepository hallTypeRepository,
            HallPricingRepository hallPricingRepository,
            AuditLogRepository auditLogRepository,
            SupabaseFileUploadService fileUploadService
    ) {
        this.hallRepository = hallRepository;
        this.hallTypeRepository = hallTypeRepository;
        this.hallPricingRepository = hallPricingRepository;
        this.auditLogRepository = auditLogRepository;
        this.fileUploadService = fileUploadService;
    }

    @Override
    public HallResponse createHall(HallRequest request, String currentUserId) {
        // BR-CH-02: Validate input - all except description must be filled
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên sảnh không được để trống");
        }

        if (request.getHallTypeId() == null) {
            throw new BadRequestException("MSG2: Loại sảnh không được để trống");
        }

        if (request.getMinTables() == null || request.getMinTables() <= 0) {
            throw new BadRequestException("MSG2: Số bàn tối thiểu phải lớn hơn 0");
        }

        if (request.getMaxTables() == null || request.getMaxTables() <= 0) {
            throw new BadRequestException("MSG2: Số bàn tối đa phải lớn hơn 0");
        }

        // BR-CH-02: Max tables must be greater than min tables
        if (request.getMaxTables() <= request.getMinTables()) {
            throw new BadRequestException("MSG59: Số bàn tối đa phải lớn hơn số bàn tối thiểu");
        }

        if (request.getHallImage() == null || request.getHallImage().isEmpty()) {
            throw new BadRequestException("MSG2: Hình ảnh sảnh không được để trống");
        }

        // Load hall type
        HallType hallType = hallTypeRepository.findById(request.getHallTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại sảnh không tồn tại"));

        // BR-CH-02: Validate that every price cell >= base price
        validatePricingMatrix(request.getPricings(), hallType.getBasePrice());

        // BR-CH-3: Check uniqueness of hall name
        if (hallRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("MSG49: Tên sảnh đã tồn tại");
        }

        // Handle image upload
        String hallImageUrl = null;
        if (request.getHallImage() != null && !request.getHallImage().isEmpty()) {
            try {
                hallImageUrl = fileUploadService.uploadPublicFile(request.getHallImage(), "halls");
                log.info("Hall image uploaded: {}", hallImageUrl);
            } catch (IOException e) {
                log.error("Error uploading hall image: {}", e.getMessage(), e);
                throw new BadRequestException("Lỗi upload hình ảnh: " + e.getMessage());
            }
        }

        // BR-CH-4: Create hall
        Hall hall = Hall.builder()
                .name(request.getName())
                .hallType(hallType)
                .minTables(request.getMinTables())
                .maxTables(request.getMaxTables())
                .hallImage(hallImageUrl)
                .description(request.getDescription())
                .status(HallStatus.INACTIVE)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Hall savedHall = hallRepository.save(hall);

        // BR-CH-4: Save pricing matrix
        savePricingMatrix(savedHall, request.getPricings());

        // Log audit
        saveAuditLog(currentUserId, "CREATE_HALL", savedHall.getId(), savedHall.getName());

        return mapToHallResponse(savedHall);
    }

    @Override
    public HallResponse updateHall(UUID hallId, HallRequest request, String currentUserId, long lastModifiedAt) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Sảnh không tồn tại"));

        if (hall.getIsDeleted()) {
            throw new ResourceNotFoundException("Sảnh đã bị xóa");
        }

        // BR-UH-02: Validate input
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên sảnh không được để trống");
        }


        if (request.getHallTypeId() == null) {
            throw new BadRequestException("MSG2: Loại sảnh không được để trống");
        }

        if (request.getMinTables() == null || request.getMinTables() <= 0) {
            throw new BadRequestException("MSG2: Số bàn tối thiểu phải lớn hơn 0");
        }

        if (request.getMaxTables() == null || request.getMaxTables() <= 0) {
            throw new BadRequestException("MSG2: Số bàn tối đa phải lớn hơn 0");
        }

        if (request.getMaxTables() <= request.getMinTables()) {
            throw new BadRequestException("MSG59: Số bàn tối đa phải lớn hơn số bàn tối thiểu");
        }

        // Check if hall type is being changed - validate it exists
        HallType hallType = hallTypeRepository.findById(request.getHallTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại sảnh không tồn tại"));

        if (!hall.getName().equals(request.getName())
                && hallRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("MSG49: Tên sảnh đã tồn tại");
        }
        // Validate pricing matrix >= base price
        validatePricingMatrix(request.getPricings(), hallType.getBasePrice());

        // BR-UH-3: Operational constraint check - check if hall in use today
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Optional<Hall> hallInUse = hallRepository.findHallInUseToday(hallId, today);

        // If hall is in use today, restrict certain updates
        if (hallInUse.isPresent()) {
            // Can only update: description, priceMatrix, hallImage
            // Cannot update: minTables, maxTables, hallTypeId
            // For simplicity, we block the update to match current project rule.
            throw new BadRequestException("MSG35: Không thể cập nhật sảnh đang được sử dụng hôm nay");
        }

        // BR-UH-4: Check uniqueness excluding current record
        if (!hall.getName().equals(request.getName())
                && hallRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("MSG49: Tên sảnh đã tồn tại");
        }

        // BR-UH-4: Optimistic locking
        if (hall.getUpdatedAt() != null && hall.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        // Handle image upload and replacement
        String hallImageUrl = hall.getHallImage();

        if (request.getHallImage() != null && !request.getHallImage().isEmpty()) {
            try {
                // Delete old image if exists
                if (hall.getHallImage() != null && !hall.getHallImage().isEmpty()) {
                    try {
                        String oldPath = extractFilePath(hall.getHallImage());
                        fileUploadService.deleteFile("public-assets", oldPath);
                        log.info("Old hall image deleted: {}", oldPath);
                    } catch (Exception e) {
                        log.warn("Failed to delete old hall image: {}", e.getMessage());
                    }
                }

                // Upload new image
                hallImageUrl = fileUploadService.uploadPublicFile(request.getHallImage(), "halls");
                log.info("Hall image updated: {}", hallImageUrl);
            } catch (IOException e) {
                log.error("Error uploading hall image: {}", e.getMessage(), e);
                throw new BadRequestException("Lỗi upload hình ảnh: " + e.getMessage());
            }
        }

        // BR-UH-5: Update hall
        hall.setName(request.getName());
        hall.setHallType(hallType);
        hall.setMinTables(request.getMinTables());
        hall.setMaxTables(request.getMaxTables());
        hall.setHallImage(hallImageUrl);
        hall.setStatus(request.getStatus());
        hall.setDescription(request.getDescription());
        hall.setUpdatedBy(currentUserId);
        hall.setUpdatedAt(Instant.now());

        Hall updatedHall = hallRepository.save(hall);

        // BR-UH-5: Update pricing matrix
        updatePricingMatrix(updatedHall, request.getPricings());

        // Log audit
        saveAuditLog(currentUserId, "UPDATE_HALL", updatedHall.getId(), updatedHall.getName());

        return mapToHallResponse(updatedHall);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> searchHalls(
            String hallName,
            UUID hallTypeId,
            Integer minTablesFrom,
            Integer maxTablesTo,
            HallStatus status
    ) {
        List<Hall> halls = hallRepository.findAllActive();

        // Apply filters
        if (hallName != null && !hallName.isBlank()) {
            halls = halls.stream()
                    .filter(h -> h.getName().toLowerCase().contains(hallName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (hallTypeId != null) {
            halls = halls.stream()
                    .filter(h -> h.getHallType().getId().equals(hallTypeId))
                    .collect(Collectors.toList());
        }

        if (minTablesFrom != null && maxTablesTo != null) {
            halls = halls.stream()
                    .filter(h -> h.getMinTables() >= minTablesFrom && h.getMaxTables() <= maxTablesTo)
                    .collect(Collectors.toList());
        }

        if (status != null) {
            halls = halls.stream()
                    .filter(h -> h.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return halls.stream()
                .map(this::mapToHallResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHall(UUID hallId, String currentUserId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Sảnh không tồn tại"));

        if (hall.getIsDeleted()) {
            throw new ResourceNotFoundException("Sảnh đã bị xóa");
        }

        // BR-DLH-3: Check for future bookings
        Instant now = Instant.now();
        long futureBookingCount = hallRepository.countFutureBookingByHall(hallId, now);

        if (futureBookingCount == 0) {
            // Case 1: Can soft delete
            hall.setIsDeleted(true);
            hall.setUpdatedBy(currentUserId);
            hall.setUpdatedAt(Instant.now());
            hallRepository.save(hall);

            // Log audit
            saveAuditLog(currentUserId, "DELETE_HALL", hallId, hall.getName());
        } else {
            // Case 2: Hall has future bookings - offer deactivation
            // MSG64: "This hall is currently assigned to future booking. You cannot delete it.
            // Do you want to deactivate this hall instead?"
            hall.setStatus(HallStatus.INACTIVE);
            hall.setUpdatedBy(currentUserId);
            hall.setUpdatedAt(Instant.now());
            hallRepository.save(hall);

            // Log audit
            saveAuditLog(currentUserId, "DEACTIVATE_HALL", hallId, hall.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponse> getAllHalls() {
        return hallRepository.findAllActive().stream()
                .map(this::mapToHallResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HallResponse getHallById(UUID hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Sảnh không tồn tại"));

        if (hall.getIsDeleted()) {
            throw new ResourceNotFoundException("Sảnh đã bị xóa");
        }

        return mapToHallResponse(hall);
    }

    private void validatePricingMatrix(List<HallPricingDTO> pricings, Double basePrice) {
        if (pricings == null || pricings.isEmpty()) {
            throw new BadRequestException("MSG2: Ma trận giá không được để trống");
        }

        // BR-CH-02: Every price must be >= base price
        for (HallPricingDTO pricing : pricings) {
            if (pricing.getPrice() == null || pricing.getPrice() < basePrice) {
                throw new BadRequestException("MSG60: Giá phòng phải lớn hơn hoặc bằng giá cơ sở");
            }
        }
    }

    private void savePricingMatrix(Hall hall, List<HallPricingDTO> pricings) {
        if (pricings != null) {
            for (HallPricingDTO pricingDTO : pricings) {
                HallPricing pricing = HallPricing.builder()
                        .hall(hall)
                        .timeSlot(pricingDTO.getTimeSlot())
                        .dayType(pricingDTO.getDayType())
                        .price(pricingDTO.getPrice())
                        .createdBy(hall.getCreatedBy())
                        .createdAt(Instant.now())
                        .build();

                hallPricingRepository.save(pricing);
            }
        }
    }

    private void updatePricingMatrix(Hall hall, List<HallPricingDTO> pricings) {
        List<HallPricing> existingPricings = hallPricingRepository.findByHallId(hall.getId());

        hallPricingRepository.deleteAll(existingPricings);
        hallPricingRepository.flush();

        if (pricings != null) {
            for (HallPricingDTO pricingDTO : pricings) {
                HallPricing pricing = HallPricing.builder()
                        .hall(hall)
                        .timeSlot(pricingDTO.getTimeSlot())
                        .dayType(pricingDTO.getDayType())
                        .price(pricingDTO.getPrice())
                        .createdBy(hall.getCreatedBy())
                        .createdAt(Instant.now())
                        .updatedBy(hall.getUpdatedBy())
                        .updatedAt(Instant.now())
                        .isDeleted(false)
                        .build();

                hallPricingRepository.save(pricing);
            }
        }
    }

    private String extractFilePath(String imageUrl) {
        // Extract path from URL:
        // https://xxxxx.supabase.co/storage/v1/object/public/public-assets/halls/filename.jpg
        // Returns: halls/filename.jpg
        if (imageUrl == null) {
            return null;
        }

        String[] parts = imageUrl.split("public-assets/");
        if (parts.length > 1) {
            return parts[1];
        }

        return imageUrl;
    }

    private HallResponse mapToHallResponse(Hall hall) {
        List<HallPricingDTO> pricings = hallPricingRepository.findByHallId(hall.getId()).stream()
                .map(p -> HallPricingDTO.builder()
                        .id(p.getId())
                        .timeSlot(p.getTimeSlot())
                        .dayType(p.getDayType())
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());

        return HallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .hallTypeId(hall.getHallType().getId())
                .hallTypeName(hall.getHallType().getName())
                .basePrice(hall.getHallType().getBasePrice())
                .minTables(hall.getMinTables())
                .maxTables(hall.getMaxTables())
                .hallImage(hall.getHallImage())
                .description(hall.getDescription())
                .status(hall.getStatus())
                .pricings(pricings)
                .lastModifiedAt(hall.getUpdatedAt())
                .lastModifiedBy(hall.getUpdatedBy())
                .build();
    }

    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) {
        try {
            UUID userUUID = UUID.fromString(userId);

            AuditLog auditLog = AuditLog.builder()
                    .userId(userUUID)
                    .action(action)
                    .targetId(targetId)
                    .targetName(targetName)
                    .createdAt(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (IllegalArgumentException e) {
            // If userId is not a valid UUID, skip audit logging to match current project style.
        }
    }
}