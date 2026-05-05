package com.wedding.management.domain.menu.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.menu.dto.DishTypeRequest;
import com.wedding.management.domain.menu.dto.DishTypeResponse;
import com.wedding.management.domain.menu.enums.DishTypeStatus;
import com.wedding.management.domain.menu.model.DishType;
import com.wedding.management.domain.menu.repository.DishRepository;
import com.wedding.management.domain.menu.repository.DishTypeRepository;
import com.wedding.management.domain.menu.service.DishTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DishTypeServiceImpl implements DishTypeService {
    private final DishTypeRepository dishTypeRepository;
    private final DishRepository dishRepository;
    private final AuditLogRepository auditLogRepository;

    public DishTypeServiceImpl(DishTypeRepository dishTypeRepository, DishRepository dishRepository, AuditLogRepository auditLogRepository) {
        this.dishTypeRepository = dishTypeRepository;
        this.dishRepository = dishRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public DishTypeResponse createDishType(DishTypeRequest request, String currentUserId) {
        // BR-CDT-02: Validate input
        validateDishTypeInput(request.getName());
        // BR-CDT-03: Check uniqueness
        if (dishTypeRepository.existsByNameAndIsDeletedFalse(request.getName())) throw new BadRequestException("MSG49: Tên loại món ăn đã tồn tại");
        // BR-CDT-04: Create dish type
        DishType dishType = DishType.builder().name(request.getName()).description(request.getDescription()).status(DishTypeStatus.ACTIVE).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
        DishType saved = dishTypeRepository.save(dishType);
        saveAuditLog(currentUserId, "CREATE_DISH_TYPE", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Override
    public DishTypeResponse updateDishType(UUID typeId, DishTypeRequest request, String currentUserId, long lastModifiedAt) {
        DishType dishType = dishTypeRepository.findById(typeId).orElseThrow(() -> new ResourceNotFoundException("Loại món ăn không tồn tại"));
        if (dishType.getIsDeleted()) throw new ResourceNotFoundException("Loại món ăn đã bị xóa");
        // BR-UDT-2: Validate input
        validateDishTypeInput(request.getName());
        // BR-UDT-3: Check uniqueness excluding current record
        if (!dishType.getName().equals(request.getName()) && dishTypeRepository.existsByNameAndIdNotAndIsDeletedFalse(request.getName(), typeId)) throw new BadRequestException("MSG49: Tên loại món ăn đã tồn tại");
        // BR-UDT-3: Optimistic locking
        if (dishType.getUpdatedAt() != null && dishType.getUpdatedAt().toEpochMilli() != lastModifiedAt) throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        // BR-UDT-4: Update dish type
        dishType.setName(request.getName()).setDescription(request.getDescription()).setStatus(request.getStatus() == null ? dishType.getStatus() : request.getStatus()).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        DishType updated = dishTypeRepository.save(dishType);
        saveAuditLog(currentUserId, "UPDATE_DISH_TYPE", updated.getId(), updated.getName());
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishTypeResponse> searchDishTypes(String nameKeyword, DishTypeStatus status) {
        List<DishType> result = dishTypeRepository.findAllActive();
        if (nameKeyword != null && !nameKeyword.isBlank()) result = result.stream().filter(t -> t.getName().toLowerCase().contains(nameKeyword.toLowerCase())).collect(Collectors.toList());
        if (status != null) result = result.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        return result.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteDishType(UUID typeId, String currentUserId, boolean deactivateIfInUse) {
        DishType dishType = dishTypeRepository.findById(typeId).orElseThrow(() -> new ResourceNotFoundException("Loại món ăn không tồn tại"));
        if (dishType.getIsDeleted()) throw new ResourceNotFoundException("Loại món ăn đã bị xóa");
        long activeDishCount = dishRepository.countActiveDishByDishType(typeId);
        // BR-DDT-3 Case 1: count = 0 -> soft delete
        if (activeDishCount == 0) {
            dishType.setIsDeleted(true);
            dishType.setUpdatedBy(currentUserId);
            dishType.setUpdatedAt(Instant.now());
            dishTypeRepository.save(dishType);
            saveAuditLog(currentUserId, "DELETE_DISH_TYPE", typeId, dishType.getName());
            return;
        }
        // BR-DDT-3 Case 2: count > 0 -> require explicit deactivation confirmation from UI
        if (!deactivateIfInUse) throw new BadRequestException("MSG: This dish type is currently assigned to dish. You cannot delete it. Do you want to deactivate this dish type instead?");
        dishType.setStatus(DishTypeStatus.INACTIVE).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        dishTypeRepository.save(dishType);
        saveAuditLog(currentUserId, "DEACTIVATE_DISH_TYPE", typeId, dishType.getName());
    }

    @Override @Transactional(readOnly = true)
    public List<DishTypeResponse> getAllDishTypes() { return dishTypeRepository.findAllActive().stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public List<DishTypeResponse> getActiveDishTypes() { return dishTypeRepository.findByStatus(DishTypeStatus.ACTIVE).stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public DishTypeResponse getDishTypeById(UUID typeId) { return mapToResponse(dishTypeRepository.findByIdAndIsDeletedFalse(typeId).orElseThrow(() -> new ResourceNotFoundException("Loại món ăn không tồn tại"))); }

    private void validateDishTypeInput(String name) { if (name == null || name.isBlank()) throw new BadRequestException("MSG2: Tên loại món ăn không được để trống"); }
    private DishTypeResponse mapToResponse(DishType t) { return DishTypeResponse.builder().id(t.getId()).name(t.getName()).description(t.getDescription()).status(t.getStatus()).lastModifiedAt(t.getUpdatedAt()).lastModifiedBy(t.getUpdatedBy()).build(); }
    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) { try { auditLogRepository.save(AuditLog.builder().userId(UUID.fromString(userId)).action(action).targetId(targetId).targetName(targetName).createdAt(Instant.now()).build()); } catch (IllegalArgumentException ignored) {} }
}
