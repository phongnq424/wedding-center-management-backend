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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;

    @Override
    @Transactional
    public HallResponse createHall(HallRequest request) {
        // Kiểm tra tên trùng (Req 8)
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
    public HallResponse updateHall(Long id, HallRequest request) {
        Hall hall = hallRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sảnh với ID: " + id));

        // Req 9: Chặn cập nhật nếu sảnh đang được sử dụng hôm nay
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
        // Chuyển đổi String status sang Enum nếu có
        HallStatus hallStatus = (status != null) ? HallStatus.valueOf(status.toUpperCase()) : null;

        return hallRepository.searchHalls(name, capacity, hallStatus)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteHall(Long id) {
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
    public HallResponse activateHall(Long id) {
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

    private boolean isHallInUseToday(Long hallId) {
        // Logic này sẽ code khi làm module Booking
        return false;
    }

    private boolean hasFutureBookings(Long hallId) {
        // Logic này sẽ code khi làm module Booking
        return false;
    }
}