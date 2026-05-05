package com.wedding.management.domain.menu.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.menu.dto.BeverageRequest;
import com.wedding.management.domain.menu.dto.BeverageResponse;
import com.wedding.management.domain.menu.enums.BeverageStatus;
import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import com.wedding.management.domain.menu.model.Beverage;
import com.wedding.management.domain.menu.model.BeverageType;
import com.wedding.management.domain.menu.repository.BeverageRepository;
import com.wedding.management.domain.menu.repository.BeverageTypeRepository;
import com.wedding.management.domain.menu.service.BeverageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BeverageServiceImpl implements BeverageService {
    private final BeverageRepository beverageRepository;
    private final BeverageTypeRepository beverageTypeRepository;
    private final AuditLogRepository auditLogRepository;

    public BeverageServiceImpl(BeverageRepository beverageRepository, BeverageTypeRepository beverageTypeRepository, AuditLogRepository auditLogRepository) {
        this.beverageRepository = beverageRepository;
        this.beverageTypeRepository = beverageTypeRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public BeverageResponse createBeverage(BeverageRequest request, String currentUserId) {
        // BR-CB-2: Validate input
        validateBeverageInput(request.getName(), request.getBeverageTypeId(), request.getUnitPrice());
        BeverageType beverageType = beverageTypeRepository.findByIdAndIsDeletedFalse(request.getBeverageTypeId()).orElseThrow(() -> new ResourceNotFoundException("Loại thức uống không tồn tại"));
        if (beverageType.getStatus() != BeverageTypeStatus.ACTIVE) throw new BadRequestException("MSG2: Loại thức uống không còn hoạt động");
        // BR-CB-3: Check uniqueness
        if (beverageRepository.existsByNameAndIsDeletedFalse(request.getName())) throw new BadRequestException("MSG49: Tên thức uống đã tồn tại");
        // BR-CB-4: Create beverage
        Beverage beverage = Beverage.builder().name(request.getName()).beverageType(beverageType).unitPrice(request.getUnitPrice()).beverageImage(request.getBeverageImage()).description(request.getDescription()).status(BeverageStatus.ACTIVE).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
        Beverage saved = beverageRepository.save(beverage);
        saveAuditLog(currentUserId, "CREATE_BEVERAGE", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Override
    public BeverageResponse updateBeverage(UUID beverageId, BeverageRequest request, String currentUserId, long lastModifiedAt) {
        Beverage beverage = beverageRepository.findById(beverageId).orElseThrow(() -> new ResourceNotFoundException("Thức uống không tồn tại"));
        if (beverage.getIsDeleted()) throw new ResourceNotFoundException("Thức uống đã bị xóa");
        // BR-UB-2: Validate input
        validateBeverageInput(request.getName(), request.getBeverageTypeId(), request.getUnitPrice());
        BeverageType beverageType = beverageTypeRepository.findByIdAndIsDeletedFalse(request.getBeverageTypeId()).orElseThrow(() -> new ResourceNotFoundException("Loại thức uống không tồn tại"));
        if (beverageType.getStatus() != BeverageTypeStatus.ACTIVE) throw new BadRequestException("MSG2: Loại thức uống không còn hoạt động");
        // BR-UB-3: Check uniqueness excluding current record
        if (!beverage.getName().equals(request.getName()) && beverageRepository.existsByNameAndIdNotAndIsDeletedFalse(request.getName(), beverageId)) throw new BadRequestException("MSG49: Tên thức uống đã tồn tại");
        // BR-UB-3: Optimistic locking
        if (beverage.getUpdatedAt() != null && beverage.getUpdatedAt().toEpochMilli() != lastModifiedAt) throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        // BR-UB-4: Update beverage; price change only affects future snapshots
        beverage.setName(request.getName()).setBeverageType(beverageType).setUnitPrice(request.getUnitPrice()).setBeverageImage(request.getBeverageImage()).setDescription(request.getDescription()).setStatus(request.getStatus() == null ? beverage.getStatus() : request.getStatus()).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        Beverage updated = beverageRepository.save(beverage);
        saveAuditLog(currentUserId, "UPDATE_BEVERAGE", updated.getId(), updated.getName());
        return mapToResponse(updated);
    }

    @Override @Transactional(readOnly = true)
    public List<BeverageResponse> searchBeverages(String beverageName, UUID beverageTypeId, Double priceFrom, Double priceTo, BeverageStatus status) {
        List<Beverage> result = beverageRepository.findAllActive();
        if (beverageName != null && !beverageName.isBlank()) result = result.stream().filter(b -> b.getName().toLowerCase().contains(beverageName.toLowerCase())).collect(Collectors.toList());
        if (beverageTypeId != null) result = result.stream().filter(b -> b.getBeverageType().getId().equals(beverageTypeId)).collect(Collectors.toList());
        if (priceFrom != null) result = result.stream().filter(b -> b.getUnitPrice() >= priceFrom).collect(Collectors.toList());
        if (priceTo != null) result = result.stream().filter(b -> b.getUnitPrice() <= priceTo).collect(Collectors.toList());
        if (status != null) result = result.stream().filter(b -> b.getStatus() == status).collect(Collectors.toList());
        return result.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteBeverage(UUID beverageId, String currentUserId, boolean deactivateIfInUse) {
        Beverage beverage = beverageRepository.findById(beverageId)
                .orElseThrow(() -> new ResourceNotFoundException("Thức uống không tồn tại"));
        if (beverage.getIsDeleted())
            throw new ResourceNotFoundException("Thức uống đã bị xóa");
        long packageCount = countActivePackageByBeverage(beverageId);
        if (packageCount == 0) {
            beverage.setIsDeleted(true);
            beverage.setUpdatedBy(currentUserId);
            beverage.setUpdatedAt(Instant.now());
            beverageRepository.save(beverage);
            saveAuditLog(currentUserId, "DELETE_BEVERAGE", beverageId, beverage.getName());
            return;
        }
        // BR-DB-3 Case 2: used in package -> require explicit deactivation confirmation
        if (!deactivateIfInUse) throw new BadRequestException("MSG: This beverage is currently assigned to wedding package. You cannot delete it. Do you want to deactivate this beverage instead?");
        beverage.setStatus(BeverageStatus.INACTIVE).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        beverageRepository.save(beverage);
        saveAuditLog(currentUserId, "DEACTIVATE_BEVERAGE", beverageId, beverage.getName());
    }

    @Override @Transactional(readOnly = true)
    public List<BeverageResponse> getAllBeverages() { return beverageRepository.findAllActive().stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public List<BeverageResponse> getActiveBeverages() { return beverageRepository.findAvailableForSelection().stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public BeverageResponse getBeverageById(UUID beverageId) { return mapToResponse(beverageRepository.findByIdAndIsDeletedFalse(beverageId).orElseThrow(() -> new ResourceNotFoundException("Thức uống không tồn tại"))); }

    private long countActivePackageByBeverage(UUID beverageId) {
        // Replace this with WeddingPackageRepository.countActivePackageByBeverage(beverageId) after the Wedding Package module is implemented.
        return 0;
    }

    private void validateBeverageInput(String name, UUID beverageTypeId, Double unitPrice) {
        if (name == null || name.isBlank()) throw new BadRequestException("MSG2: Tên thức uống không được để trống");
        if (beverageTypeId == null) throw new BadRequestException("MSG2: Loại thức uống không được để trống");
        if (unitPrice == null) throw new BadRequestException("MSG2: Đơn giá không được để trống");
        if (unitPrice <= 0) throw new BadRequestException("MSG13: Đơn giá phải lớn hơn 0");
    }
    private BeverageResponse mapToResponse(Beverage b) { return BeverageResponse.builder().id(b.getId()).name(b.getName()).beverageTypeId(b.getBeverageType().getId()).beverageTypeName(b.getBeverageType().getName()).unitPrice(b.getUnitPrice()).beverageImage(b.getBeverageImage()).description(b.getDescription()).status(b.getStatus()).lastModifiedAt(b.getUpdatedAt()).lastModifiedBy(b.getUpdatedBy()).build(); }
    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) { try { auditLogRepository.save(AuditLog.builder().userId(UUID.fromString(userId)).action(action).targetId(targetId).targetName(targetName).createdAt(Instant.now()).build()); } catch (IllegalArgumentException ignored) {} }
}
