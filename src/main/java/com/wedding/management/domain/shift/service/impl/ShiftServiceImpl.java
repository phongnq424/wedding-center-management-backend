package com.wedding.management.domain.shift.service.impl;

import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.shift.dto.ShiftRequest;
import com.wedding.management.domain.shift.dto.ShiftResponse;
import com.wedding.management.domain.shift.enums.ShiftStatus;
import com.wedding.management.domain.shift.model.Shift;
import com.wedding.management.domain.shift.repository.ShiftRepository;
import com.wedding.management.domain.shift.repository.ShiftSpecifications;
import com.wedding.management.domain.shift.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;

    @Override
    @Transactional
    public ShiftResponse createShift(ShiftRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (shiftRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("Tên ca '" + request.getName() + "' đã tồn tại!");
        }

        Shift shift = Shift.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .description(request.getDescription())
                .status(ShiftStatus.ACTIVE)
                .build();

        return mapToResponse(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public ShiftResponse updateShift(UUID id, ShiftRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        Shift shift = shiftRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca với ID: " + id));

        shift.setName(request.getName())
                .setStartTime(request.getStartTime())
                .setEndTime(request.getEndTime())
                .setDescription(request.getDescription());

        return mapToResponse(shiftRepository.save(shift));
    }

    @Override
    public List<ShiftResponse> searchShifts(String name, LocalTime fromTime, LocalTime toTime, String status) {
        ShiftStatus shiftStatus = null;
        if (status != null && !status.isEmpty()) {
            try { shiftStatus = ShiftStatus.valueOf(status.toUpperCase()); } catch (Exception ignored) {}
        }

        Specification<Shift> spec = ShiftSpecifications.filterShifts(name, fromTime, toTime, shiftStatus);
        return shiftRepository.findAll(spec).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteShift(UUID id) {
        Shift shift = shiftRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca để xóa!"));

        // Check logic: Nếu ca đang có lịch phân công (Assignment) thì không cho xóa
        if (isShiftCurrentlyAssigned(id)) {
            throw new BadRequestException("Ca này đang được phân công/sử dụng, không thể xóa!");
        }

        shift.setIsDeleted(true);
        shiftRepository.save(shift);
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Giờ bắt đầu phải trước giờ kết thúc!");
        }
    }

    private boolean isShiftCurrentlyAssigned(UUID shiftId) {
        // Logic sẽ check bảng Assignment hoặc Booking sau này
        return false;
    }

    private ShiftResponse mapToResponse(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .status(shift.getStatus().name())
                .description(shift.getDescription())
                .build();
    }
}