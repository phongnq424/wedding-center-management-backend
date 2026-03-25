package com.wedding.management.domain.hall.service.impl;

import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.domain.hall.dto.HallRequest;
import com.wedding.management.domain.hall.dto.HallResponse;
import com.wedding.management.domain.hall.enums.HallStatus;
import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.hall.repository.HallRepository;
import com.wedding.management.domain.hall.service.HallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import com.wedding.management.domain.hall.repository.HallSpecifications;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;

    @Override
    @Transactional
    public HallResponse createHall(HallRequest request) {
        if (hallRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("Tên sảnh '" + request.getName() + "' đã tồn tại!");
        }

        Hall hall = Hall.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .basePrice(request.getBasePrice())
                .description(request.getDescription())
                .status(HallStatus.INACTIVE) // Mặc định là Inactive (Req 8)
                .build();

        log.info("Đang tạo sảnh mới: {}", hall.getName());
        return mapToResponse(hallRepository.save(hall));
    }

    @Override
    @Transactional
    public HallResponse updateHall(UUID id, HallRequest request) {
        Hall hall = hallRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sảnh với ID: " + id));
        if (isHallInUseToday(id)) {
            throw new BadRequestException("Sảnh đang trong ca tiệc hôm nay, không thể chỉnh sửa!");
        }

        hall.setName(request.getName())
                .setCapacity(request.getCapacity())
                .setBasePrice(request.getBasePrice())
                .setDescription(request.getDescription());

        return mapToResponse(hallRepository.save(hall));
    }

    @Override
    public List<HallResponse> searchHalls(String name, Integer capacity, String status) {
        HallStatus hallStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                hallStatus = HallStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Invalid status provided: {}", status);
            }
        }

        Specification<Hall> spec = HallSpecifications.filterHalls(name, capacity, hallStatus);

        return hallRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteHall(UUID id) {
        Hall hall = hallRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sảnh để xóa!"));

        // Req 11: Chặn xóa nếu có lịch đặt trong tương lai
        if (hasFutureBookings(id)) {
            throw new BadRequestException("Sảnh còn lịch đặt tiệc trong tương lai, không thể xóa!");
        }

        hall.setIsDeleted(true); // Soft Delete
        hallRepository.save(hall);
        log.warn("Đã xóa mềm sảnh ID: {}", id);
    }

    @Override
    @Transactional
    public HallResponse activateHall(UUID id) {
        Hall hall = hallRepository.findByIdAndIsDeletedFalse(id).orElseThrow();
        hall.setStatus(HallStatus.ACTIVE);
        return mapToResponse(hallRepository.save(hall));
    }

    // --- Helper Methods (Các hàm bổ trợ) ---

    private HallResponse mapToResponse(Hall hall) {
        return HallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .capacity(hall.getCapacity())
                .basePrice(hall.getBasePrice())
                .status(hall.getStatus().name())
                .description(hall.getDescription())
                .build();
    }

    private boolean isHallInUseToday(UUID hallId) {
        // Logic này sẽ code khi làm module Booking
        return false;
    }

    private boolean hasFutureBookings(UUID hallId) {
        // Logic này sẽ code khi làm module Booking
        return false;
    }
}