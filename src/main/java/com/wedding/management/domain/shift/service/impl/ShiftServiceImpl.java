package com.wedding.management.domain.shift.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.shift.dto.ShiftRequest;
import com.wedding.management.domain.shift.dto.ShiftResponse;
import com.wedding.management.domain.shift.enums.ShiftStatus;
import com.wedding.management.domain.shift.model.Shift;
import com.wedding.management.domain.shift.repository.ShiftRepository;
import com.wedding.management.domain.shift.service.ShiftService;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final AuditLogRepository auditLogRepository;

    public ShiftServiceImpl(ShiftRepository shiftRepository, AuditLogRepository auditLogRepository) {
        this.shiftRepository = shiftRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public ShiftResponse createShift(ShiftRequest request, String currentUserId) {
        // BR-CSF-2: Validate input
        validateShiftInput(request);

        // BR-CSF-3: Check uniqueness
        if (shiftRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("MSG49: Tên ca đã tồn tại");
        }

        // BR-CSF-3: Check time overlap
        if (!shiftRepository.findOverlappingShifts(request.getStartTime(), request.getEndTime(), null, true).isEmpty()) {
            throw new BadRequestException("MSG36: Thời gian ca bị trùng với ca hiện có");
        }

        // BR-CSF-4: Create shift
        Shift shift = Shift.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ShiftStatus.ACTIVE)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Shift savedShift = shiftRepository.save(shift);

        // Log audit
        saveAuditLog(currentUserId, "CREATE_SHIFT", savedShift.getId(), savedShift.getName());

        return mapToShiftResponse(savedShift);
    }

    @Override
    public ShiftResponse updateShift(UUID shiftId, ShiftRequest request, String currentUserId, long lastModifiedAt) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Ca không tồn tại"));

        if (shift.getIsDeleted()) {
            throw new ResourceNotFoundException("Ca đã bị xóa");
        }

        // BR-USF-2: Validate input
        validateShiftInput(request);

        // BR-USF-4: Check uniqueness excluding current record
        if (!shift.getName().equals(request.getName()) && shiftRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("MSG49: Tên ca đã tồn tại");
        }

        // BR-USF-3: Check time overlap excluding current record
        if (!shiftRepository.findOverlappingShifts(request.getStartTime(), request.getEndTime(), shiftId, true).isEmpty()) {
            throw new BadRequestException("MSG36: Thời gian ca bị trùng với ca hiện có");
        }

        // BR-USF-4: Optimistic locking
        if (shift.getUpdatedAt() != null && shift.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        // BR-USF-5: Update shift
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setStatus(request.getStatus() != null ? request.getStatus() : shift.getStatus());
        shift.setUpdatedBy(currentUserId);
        shift.setUpdatedAt(Instant.now());

        Shift updatedShift = shiftRepository.save(shift);

        // Log audit
        saveAuditLog(currentUserId, "UPDATE_SHIFT", updatedShift.getId(), updatedShift.getName());

        return mapToShiftResponse(updatedShift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> searchShifts(String shiftName, LocalTime startTimeFrom, LocalTime endTimeTo, ShiftStatus status) {
        List<Shift> shifts = shiftRepository.findAllActive();

        // BR-SSH-3: Search filters combined using AND logic
        if (shiftName != null && !shiftName.isBlank()) {
            shifts = shifts.stream()
                    .filter(s -> s.getName().toLowerCase().contains(shiftName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (startTimeFrom != null) {
            shifts = shifts.stream()
                    .filter(s -> !s.getStartTime().isBefore(startTimeFrom))
                    .collect(Collectors.toList());
        }

        if (endTimeTo != null) {
            shifts = shifts.stream()
                    .filter(s -> !s.getEndTime().isAfter(endTimeTo))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            shifts = shifts.stream()
                    .filter(s -> s.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return shifts.stream()
                .map(this::mapToShiftResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteShift(UUID shiftId, String currentUserId, boolean deactivateIfInUse) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Ca không tồn tại"));

        if (shift.getIsDeleted()) {
            throw new ResourceNotFoundException("Ca đã bị xóa");
        }

        // BR-DSF-4: Count future booking by shift
        long futureBookingCount = countFutureBookingByShift(shiftId);

        if (futureBookingCount == 0) {
            // Case 1: Soft delete shift
            shift.setIsDeleted(true);
            shift.setDeletedBy(currentUserId);
            shift.setDeletedAt(Instant.now());
            shift.setUpdatedBy(currentUserId);
            shift.setUpdatedAt(Instant.now());
            shiftRepository.save(shift);

            saveAuditLog(currentUserId, "DELETE_SHIFT", shiftId, shift.getName());
        } else {
            // Case 2: Shift is in use - only deactivate if user confirms from UI
            if (!deactivateIfInUse) {
                throw new BadRequestException("MSG64: This shift is currently assigned to future booking. You cannot delete it. Do you want to deactivate this shift instead?");
            }

            shift.setStatus(ShiftStatus.INACTIVE);
            shift.setUpdatedBy(currentUserId);
            shift.setUpdatedAt(Instant.now());
            shiftRepository.save(shift);

            saveAuditLog(currentUserId, "DEACTIVATE_SHIFT", shiftId, shift.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getAllShifts() {
        return shiftRepository.findAllActive().stream()
                .map(this::mapToShiftResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getActiveShifts() {
        return shiftRepository.findByStatus(ShiftStatus.ACTIVE).stream()
                .map(this::mapToShiftResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Ca không tồn tại"));

        if (shift.getIsDeleted()) {
            throw new ResourceNotFoundException("Ca đã bị xóa");
        }

        return mapToShiftResponse(shift);
    }

    private void validateShiftInput(ShiftRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên ca không được để trống");
        }

        if (request.getStartTime() == null) {
            throw new BadRequestException("MSG2: Thời gian bắt đầu không được để trống");
        }

        if (request.getEndTime() == null) {
            throw new BadRequestException("MSG2: Thời gian kết thúc không được để trống");
        }

        // BR-CSF-2 / BR-USF-2: startTime must be less than endTime
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("MSG65: Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc");
        }
    }

    private long countFutureBookingByShift(UUID shiftId) {
        // Booking module is not available yet. Keep this placeholder to avoid UnknownEntityException.
        // Replace this with BookingRepository.countFutureBookingByShift(shiftId, Instant.now()) when Booking is implemented.
        return 0;
    }

    private ShiftResponse mapToShiftResponse(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .status(shift.getStatus())
                .lastModifiedAt(shift.getUpdatedAt())
                .lastModifiedBy(shift.getUpdatedBy())
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
