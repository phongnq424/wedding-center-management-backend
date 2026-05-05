package com.wedding.management.domain.hall.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.hall.dto.HallTypeRequest;
import com.wedding.management.domain.hall.dto.HallTypeResponse;
import com.wedding.management.domain.hall.enums.HallTypeStatus;
import com.wedding.management.domain.hall.model.HallType;
import com.wedding.management.domain.hall.repository.HallTypeRepository;
import com.wedding.management.domain.hall.service.HallTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class HallTypeServiceImpl implements HallTypeService {

    private final HallTypeRepository hallTypeRepository;
    private final AuditLogRepository auditLogRepository;

    public HallTypeServiceImpl(HallTypeRepository hallTypeRepository, AuditLogRepository auditLogRepository) {
        this.hallTypeRepository = hallTypeRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public HallTypeResponse createHallType(HallTypeRequest request, String currentUserId) {
        // BR-CHT-02: Validate input - all except description must be filled
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên loại sảnh không được để trống");
        }

        if (request.getBasePrice() == null || request.getBasePrice() <= 0) {
            throw new BadRequestException("MSG13: Giá cơ sở phải lớn hơn 0");
        }

        // BR-CHT-03: Check uniqueness
        Optional<HallType> existingType = hallTypeRepository.findByName(request.getName());
        if (existingType.isPresent() && !existingType.get().getIsDeleted()) {
            throw new BadRequestException("MSG49: Tên loại sảnh đã tồn tại");
        }

        // BR-CHT-04: Create hall type
        HallType hallType = HallType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(HallTypeStatus.ACTIVE)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        HallType savedHallType = hallTypeRepository.save(hallType);

        // Log audit
        saveAuditLog(currentUserId, "CREATE_HALL_TYPE", savedHallType.getId(), savedHallType.getName());

        return mapToHallTypeResponse(savedHallType);
    }

    @Override
    public HallTypeResponse updateHallType(UUID hallTypeId, HallTypeRequest request, String currentUserId, long lastModifiedAt) {
        HallType hallType = hallTypeRepository.findById(hallTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loại sảnh không tồn tại"));

        if (hallType.getIsDeleted()) {
            throw new ResourceNotFoundException("Loại sảnh đã bị xóa");
        }

        // BR-UPHT-2: Validate input
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên loại sảnh không được để trống");
        }

        if (request.getBasePrice() == null || request.getBasePrice() <= 0) {
            throw new BadRequestException("MSG13: Giá cơ sở phải lớn hơn 0");
        }

        // BR-UPHT-3: Check uniqueness (excluding current record)
        if (!hallType.getName().equals(request.getName())) {
            Optional<HallType> existingType = hallTypeRepository.findByName(request.getName());
            if (existingType.isPresent() && !existingType.get().getIsDeleted()) {
                throw new BadRequestException("MSG49: Tên loại sảnh đã tồn tại");
            }
        }

        // BR-UPHT-3: Optimistic locking - check version conflict
        if (hallType.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        // BR-UPHT-4: Update hall type
        hallType.setName(request.getName());
        hallType.setDescription(request.getDescription());
        hallType.setBasePrice(request.getBasePrice());
        hallType.setUpdatedBy(currentUserId);
        hallType.setUpdatedAt(Instant.now());

        HallType updatedHallType = hallTypeRepository.save(hallType);

        // Log audit
        saveAuditLog(currentUserId, "UPDATE_HALL_TYPE", updatedHallType.getId(), updatedHallType.getName());

        return mapToHallTypeResponse(updatedHallType);
    }

    @Override
    public List<HallTypeResponse> searchHallTypes(String nameKeyword, Double minBasePrice, HallTypeStatus status) {
        List<HallType> hallTypes;

        // BR-SEHT-03: Search with filters combined using AND logic
        if (nameKeyword != null && !nameKeyword.isBlank() && minBasePrice != null && status != null) {
            hallTypes = hallTypeRepository.searchByNameAndStatus(nameKeyword, status);
            // Additional filter by minimum price in memory (not in query for simplicity)
            hallTypes = hallTypes.stream()
                    .filter(ht -> ht.getBasePrice() >= minBasePrice)
                    .collect(Collectors.toList());
        } else if (nameKeyword != null && !nameKeyword.isBlank() && minBasePrice != null) {
            hallTypes = hallTypeRepository.searchByNameAndMinPrice(nameKeyword, minBasePrice);
        } else if (nameKeyword != null && !nameKeyword.isBlank() && status != null) {
            hallTypes = hallTypeRepository.searchByNameAndStatus(nameKeyword, status);
        } else if (nameKeyword != null && !nameKeyword.isBlank()) {
            hallTypes = hallTypeRepository.searchByName(nameKeyword);
        } else if (status != null) {
            hallTypes = hallTypeRepository.findByStatus(status);
        } else {
            // BR-SEHT-01: Default - retrieve all sorted by last modified DESC
            hallTypes = hallTypeRepository.findAllActive();
        }

        // BR-SEHT-04: Map to response
        return hallTypes.stream()
                .map(this::mapToHallTypeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHallType(UUID hallTypeId, String currentUserId) {
        HallType hallType = hallTypeRepository.findById(hallTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loại sảnh không tồn tại"));

        if (hallType.getIsDeleted()) {
            throw new ResourceNotFoundException("Loại sảnh đã bị xóa");
        }

        // BR-DLHT-3: Check if hall type is assigned to halls
        long hallCount = hallTypeRepository.countActiveHallByHallType(hallTypeId);

        if (hallCount == 0) {
            // Case 1: Can soft delete
            hallType.setIsDeleted(true);
            hallType.setUpdatedBy(currentUserId);
            hallType.setUpdatedAt(Instant.now());
            hallTypeRepository.save(hallType);

            // Log audit
            saveAuditLog(currentUserId, "DELETE_HALL_TYPE", hallTypeId, hallType.getName());
        } else {
            // Case 2: Hall type is in use - offer deactivation
            // MSG: "This hall type is currently assigned to hall. You cannot delete it. Do you want to deactivate this hall type instead?"
            // If user confirms deactivation
            hallType.setStatus(HallTypeStatus.INACTIVE);
            hallType.setUpdatedBy(currentUserId);
            hallType.setUpdatedAt(Instant.now());
            hallTypeRepository.save(hallType);

            // Log audit
            saveAuditLog(currentUserId, "DEACTIVATE_HALL_TYPE", hallTypeId, hallType.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallTypeResponse> getAllHallTypes() {
        return hallTypeRepository.findAllActive().stream()
                .map(this::mapToHallTypeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HallTypeResponse getHallTypeById(UUID hallTypeId) {
        HallType hallType = hallTypeRepository.findById(hallTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loại sảnh không tồn tại"));

        if (hallType.getIsDeleted()) {
            throw new ResourceNotFoundException("Loại sảnh đã bị xóa");
        }

        return mapToHallTypeResponse(hallType);
    }

    private HallTypeResponse mapToHallTypeResponse(HallType hallType) {
        return HallTypeResponse.builder()
                .id(hallType.getId())
                .name(hallType.getName())
                .description(hallType.getDescription())
                .basePrice(hallType.getBasePrice())
                .status(hallType.getStatus())
                .lastModifiedAt(hallType.getUpdatedAt())
                .lastModifiedBy(hallType.getUpdatedBy())
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
            // If userId is not a valid UUID, skip audit logging
        }
    }
}
