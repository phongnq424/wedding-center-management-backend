package com.wedding.management.domain.service.service.impl;

import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.service.dto.ServiceRequest;
import com.wedding.management.domain.service.dto.ServiceResponse;
import com.wedding.management.domain.service.enums.ServiceStatus;
import com.wedding.management.domain.service.model.WeddingService;
import com.wedding.management.domain.service.repository.ServiceRepository;
import com.wedding.management.domain.service.repository.ServiceSpecifications;
import com.wedding.management.domain.service.service.WeddingServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeddingServiceServiceImpl implements WeddingServiceService {

    private final ServiceRepository serviceRepository;

    @Override
    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        if (serviceRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("Dịch vụ '" + request.getName() + "' đã tồn tại!");
        }

        WeddingService service = WeddingService.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .status(ServiceStatus.ACTIVE) // Sẵn sàng ngay khi tạo
                .build();

        return mapToResponse(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public ServiceResponse updateService(UUID id, ServiceRequest request) {
        WeddingService service = serviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ ID: " + id));

        // Logic Price Retroactive: Thực tế giá trong Booking sẽ được lưu snapshot vào bảng phụ
        // nên việc update giá ở đây chỉ ảnh hưởng đến các booking tạo mới từ bây giờ.
        service.setName(request.getName())
                .setPrice(request.getPrice())
                .setDescription(request.getDescription());

        return mapToResponse(serviceRepository.save(service));
    }

    @Override
    public List<ServiceResponse> searchServices(String name, String category, String status) {
        ServiceStatus st = null;
        try {
            if (status != null) st = ServiceStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            log.error("Lỗi parse enum search: {}", e.getMessage());
        }

        Specification<WeddingService> spec = ServiceSpecifications.filterServices(name, st);
        return serviceRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteService(UUID id) {
        WeddingService service = serviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại!"));

        if (isServiceInActiveBooking(id)) {
            throw new BadRequestException("Dịch vụ đang nằm trong các đơn đặt tiệc chưa hoàn tất, không thể xóa!");
        }

        service.setIsDeleted(true);
        serviceRepository.save(service);
    }

    private boolean isServiceInActiveBooking(UUID serviceId) {
        // Sẽ check bảng BookingService sau
        return false;
    }

    private ServiceResponse mapToResponse(WeddingService service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .price(service.getPrice())
                .status(service.getStatus().name())
                .description(service.getDescription())
                .build();
    }
}