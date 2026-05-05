package com.wedding.management.domain.service.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.config.supabase.SupabaseFileUploadService;
import com.wedding.management.domain.service.dto.ServiceRequest;
import com.wedding.management.domain.service.dto.ServiceResponse;
import com.wedding.management.domain.service.enums.ServiceStatus;
import com.wedding.management.domain.service.model.Service;
import com.wedding.management.domain.service.repository.ServiceRepository;
import com.wedding.management.domain.service.service.ServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
@Slf4j
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final AuditLogRepository auditLogRepository;
    private final SupabaseFileUploadService fileUploadService;

    public ServiceServiceImpl(ServiceRepository serviceRepository, AuditLogRepository auditLogRepository,
            SupabaseFileUploadService fileUploadService) {
        this.serviceRepository = serviceRepository;
        this.auditLogRepository = auditLogRepository;
        this.fileUploadService = fileUploadService;
    }

    @Override
    public ServiceResponse createService(ServiceRequest request, String currentUserId) {
        // BR-CSF-2: Validate input
        validateServiceInput(request);

        // BR-CSF-3: Check uniqueness
        if (serviceRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("MSG49: Tên dịch vụ đã tồn tại");
        }

        // Handle image upload
        String serviceImageUrl = null;
        if (request.getServiceImage() != null && !request.getServiceImage().isEmpty()) {
            try {
                serviceImageUrl = fileUploadService.uploadPublicFile(request.getServiceImage(), "services");
                log.info("Service image uploaded: {}", serviceImageUrl);
            } catch (IOException e) {
                log.error("Error uploading service image: {}", e.getMessage(), e);
                throw new BadRequestException("Lỗi upload hình ảnh: " + e.getMessage());
            }
        }

        // BR-CSF-4: Create service
        Service service = Service.builder()
                .name(request.getName())
                .price(request.getPrice())
                .serviceImage(serviceImageUrl)
                .description(request.getDescription())
                .status(ServiceStatus.ACTIVE)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Service savedService = serviceRepository.save(service);

        // Log audit
        saveAuditLog(currentUserId, "CREATE_SERVICE", savedService.getId(), savedService.getName());

        return mapToServiceResponse(savedService);
    }

    @Override
    public ServiceResponse updateService(UUID serviceId, ServiceRequest request, String currentUserId,
            long lastModifiedAt) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

        if (service.getIsDeleted()) {
            throw new ResourceNotFoundException("Dịch vụ đã bị xóa");
        }

        // BR-USV-2: Validate input
        validateServiceInput(request);

        // BR-USV-3: Check uniqueness excluding current record
        if (!service.getName().equals(request.getName())
                && serviceRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new BadRequestException("MSG49: Tên dịch vụ đã tồn tại");
        }

        // BR-USV-3: Optimistic locking
        if (service.getUpdatedAt() != null && service.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        // Handle image upload and replacement
        String serviceImageUrl = service.getServiceImage();
        if (request.getServiceImage() != null && !request.getServiceImage().isEmpty()) {
            try {
                // Delete old image if exists
                if (service.getServiceImage() != null && !service.getServiceImage().isEmpty()) {
                    try {
                        String oldPath = extractFilePath(service.getServiceImage());
                        fileUploadService.deleteFile("public-assets", oldPath);
                        log.info("Old service image deleted: {}", oldPath);
                    } catch (Exception e) {
                        log.warn("Failed to delete old image: {}", e.getMessage());
                    }
                }

                // Upload new image
                serviceImageUrl = fileUploadService.uploadPublicFile(request.getServiceImage(), "services");
                log.info("Service image updated: {}", serviceImageUrl);
            } catch (IOException e) {
                log.error("Error uploading service image: {}", e.getMessage(), e);
                throw new BadRequestException("Lỗi upload hình ảnh: " + e.getMessage());
            }
        }

        // BR-USV-4: Update service
        service.setName(request.getName());
        service.setPrice(request.getPrice());
        service.setServiceImage(serviceImageUrl);
        service.setDescription(request.getDescription());
        service.setStatus(request.getStatus() != null ? request.getStatus() : service.getStatus());
        service.setUpdatedBy(currentUserId);
        service.setUpdatedAt(Instant.now());

        Service updatedService = serviceRepository.save(service);

        // Log audit
        saveAuditLog(currentUserId, "UPDATE_SERVICE", updatedService.getId(), updatedService.getName());

        return mapToServiceResponse(updatedService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> searchServices(String nameKeyword, Double minPrice, ServiceStatus status) {
        List<Service> services = serviceRepository.findAllActive();

        // BR-SSV-2: Search filters combined using AND logic
        if (nameKeyword != null && !nameKeyword.isBlank()) {
            services = services.stream()
                    .filter(s -> s.getName().toLowerCase().contains(nameKeyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            services = services.stream()
                    .filter(s -> s.getPrice() >= minPrice)
                    .collect(Collectors.toList());
        }

        if (status != null) {
            services = services.stream()
                    .filter(s -> s.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return services.stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteService(UUID serviceId, String currentUserId, boolean deactivateIfInUse) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

        if (service.getIsDeleted()) {
            throw new ResourceNotFoundException("Dịch vụ đã bị xóa");
        }

        // BR-DSV-3: Count package by service
        long packageCount = serviceRepository.countPackageByService(serviceId);

        if (packageCount == 0) {
            // Case 1: Soft delete service
            service.setIsDeleted(true);
            service.setDeletedBy(currentUserId);
            service.setDeletedAt(Instant.now());
            service.setUpdatedBy(currentUserId);
            service.setUpdatedAt(Instant.now());
            serviceRepository.save(service);

            saveAuditLog(currentUserId, "DELETE_SERVICE", serviceId, service.getName());
        } else {
            // Case 2: Service is in wedding package - only deactivate if user confirms from
            // UI
            if (!deactivateIfInUse) {
                throw new BadRequestException(
                        "This service is currently assigned to wedding package. You cannot delete it. Do you want to deactivate this service instead or check your package?");
            }

            service.setStatus(ServiceStatus.INACTIVE);
            service.setUpdatedBy(currentUserId);
            service.setUpdatedAt(Instant.now());
            serviceRepository.save(service);

            saveAuditLog(currentUserId, "DEACTIVATE_SERVICE", serviceId, service.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAllActive().stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getActiveServices() {
        return serviceRepository.findByStatus(ServiceStatus.ACTIVE).stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(UUID serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

        if (service.getIsDeleted()) {
            throw new ResourceNotFoundException("Dịch vụ đã bị xóa");
        }

        return mapToServiceResponse(service);
    }

    private void validateServiceInput(ServiceRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("MSG2: Tên dịch vụ không được để trống");
        }

        if (request.getPrice() == null) {
            throw new BadRequestException("MSG2: Giá dịch vụ không được để trống");
        }

        if (request.getPrice() <= 0) {
            throw new BadRequestException("MSG13: Giá dịch vụ phải lớn hơn 0");
        }
    }

    private String extractFilePath(String imageUrl) {
        // Extract path from URL:
        // https://xxxxx.supabase.co/storage/v1/object/public/public-assets/services/filename.jpg
        // Returns: services/filename.jpg
        if (imageUrl == null)
            return null;
        String[] parts = imageUrl.split("public-assets/");
        if (parts.length > 1) {
            return parts[1];
        }
        return imageUrl;
    }

    private ServiceResponse mapToServiceResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .price(service.getPrice())
                .serviceImage(service.getServiceImage())
                .description(service.getDescription())
                .status(service.getStatus())
                .lastModifiedAt(service.getUpdatedAt())
                .lastModifiedBy(service.getUpdatedBy())
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
            // If userId is not a valid UUID, skip audit logging to match current project
            // style.
        }
    }
}
