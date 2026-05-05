package com.wedding.management.domain.menu.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.menu.dto.BeverageTypeRequest;
import com.wedding.management.domain.menu.dto.BeverageTypeResponse;
import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import com.wedding.management.domain.menu.model.BeverageType;
import com.wedding.management.domain.menu.repository.BeverageRepository;
import com.wedding.management.domain.menu.repository.BeverageTypeRepository;
import com.wedding.management.domain.menu.service.BeverageTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BeverageTypeServiceImpl implements BeverageTypeService {
    private final BeverageTypeRepository beverageTypeRepository;
    private final BeverageRepository beverageRepository;
    private final AuditLogRepository auditLogRepository;

    public BeverageTypeServiceImpl(BeverageTypeRepository beverageTypeRepository, BeverageRepository beverageRepository, AuditLogRepository auditLogRepository) {
        this.beverageTypeRepository = beverageTypeRepository;
        this.beverageRepository = beverageRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public BeverageTypeResponse createBeverageType(BeverageTypeRequest request, String currentUserId) {
        // BR-CBT-02: Validate input
        validateBeverageTypeInput(request.getName());
        // BR-CBT-03: Check uniqueness
        if (beverageTypeRepository.existsByNameAndIsDeletedFalse(request.getName())) throw new BadRequestException("MSG49: Tên loại thức uống đã tồn tại");
        // BR-CBT-04: Create beverage type
        BeverageType beverageType = BeverageType.builder().name(request.getName()).description(request.getDescription()).status(BeverageTypeStatus.ACTIVE).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
        BeverageType saved = beverageTypeRepository.save(beverageType);
        saveAuditLog(currentUserId, "CREATE_BEVERAGE_TYPE", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Override
    public BeverageTypeResponse updateBeverageType(UUID typeId, BeverageTypeRequest request, String currentUserId, long lastModifiedAt) {
        BeverageType beverageType = beverageTypeRepository.findById(typeId).orElseThrow(() -> new ResourceNotFoundException("Loại thức uống không tồn tại"));
        if (beverageType.getIsDeleted()) throw new ResourceNotFoundException("Loại thức uống đã bị xóa");
        // BR-UBT-2: Validate input
        validateBeverageTypeInput(request.getName());
        // BR-UBT-3: Check uniqueness excluding current record
        if (!beverageType.getName().equals(request.getName()) && beverageTypeRepository.existsByNameAndIdNotAndIsDeletedFalse(request.getName(), typeId)) throw new BadRequestException("MSG49: Tên loại thức uống đã tồn tại");
        // BR-UBT-3: Optimistic locking
        if (beverageType.getUpdatedAt() != null && beverageType.getUpdatedAt().toEpochMilli() != lastModifiedAt) throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        // BR-UBT-4: Update beverage type
        beverageType.setName(request.getName()).setDescription(request.getDescription()).setStatus(request.getStatus() == null ? beverageType.getStatus() : request.getStatus()).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        BeverageType updated = beverageTypeRepository.save(beverageType);
        saveAuditLog(currentUserId, "UPDATE_BEVERAGE_TYPE", updated.getId(), updated.getName());
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeverageTypeResponse> searchBeverageTypes(String nameKeyword, BeverageTypeStatus status) {
        List<BeverageType> result = beverageTypeRepository.findAllActive();
        if (nameKeyword != null && !nameKeyword.isBlank()) result = result.stream().filter(t -> t.getName().toLowerCase().contains(nameKeyword.toLowerCase())).collect(Collectors.toList());
        if (status != null) result = result.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        return result.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteBeverageType(UUID typeId, String currentUserId, boolean deactivateIfInUse) {
        BeverageType beverageType = beverageTypeRepository.findById(typeId).orElseThrow(() -> new ResourceNotFoundException("Loại thức uống không tồn tại"));
        if (beverageType.getIsDeleted()) throw new ResourceNotFoundException("Loại thức uống đã bị xóa");
        long activeBeverageCount = beverageRepository.countActiveBeverageByBeverageType(typeId);
        // BR-DBT-3 Case 1: count = 0 -> soft delete
        if (activeBeverageCount == 0) {
            beverageType.setIsDeleted(true);
            beverageType.setUpdatedBy(currentUserId);
            beverageType.setUpdatedAt(Instant.now());
            beverageTypeRepository.save(beverageType);
            saveAuditLog(currentUserId, "DELETE_BEVERAGE_TYPE", typeId, beverageType.getName());
            return;
        }
        // BR-DBT-3 Case 2: count > 0 -> require explicit deactivation confirmation from UI
        if (!deactivateIfInUse) throw new BadRequestException("MSG: This beverage type is currently assigned to beverage. You cannot delete it. Do you want to deactivate this beverage type instead?");
        beverageType.setStatus(BeverageTypeStatus.INACTIVE).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        beverageTypeRepository.save(beverageType);
        saveAuditLog(currentUserId, "DEACTIVATE_BEVERAGE_TYPE", typeId, beverageType.getName());
    }

    @Override @Transactional(readOnly = true)
    public List<BeverageTypeResponse> getAllBeverageTypes() { return beverageTypeRepository.findAllActive().stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public List<BeverageTypeResponse> getActiveBeverageTypes() { return beverageTypeRepository.findByStatus(BeverageTypeStatus.ACTIVE).stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public BeverageTypeResponse getBeverageTypeById(UUID typeId) { return mapToResponse(beverageTypeRepository.findByIdAndIsDeletedFalse(typeId).orElseThrow(() -> new ResourceNotFoundException("Loại thức uống không tồn tại"))); }

    private void validateBeverageTypeInput(String name) { if (name == null || name.isBlank()) throw new BadRequestException("MSG2: Tên loại thức uống không được để trống"); }
    private BeverageTypeResponse mapToResponse(BeverageType t) { return BeverageTypeResponse.builder().id(t.getId()).name(t.getName()).description(t.getDescription()).status(t.getStatus()).lastModifiedAt(t.getUpdatedAt()).lastModifiedBy(t.getUpdatedBy()).build(); }
    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) { try { auditLogRepository.save(AuditLog.builder().userId(UUID.fromString(userId)).action(action).targetId(targetId).targetName(targetName).createdAt(Instant.now()).build()); } catch (IllegalArgumentException ignored) {} }
}
