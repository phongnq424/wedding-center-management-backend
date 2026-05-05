package com.wedding.management.domain.menu.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.menu.dto.*;
import com.wedding.management.domain.menu.enums.*;
import com.wedding.management.domain.menu.model.*;
import com.wedding.management.domain.menu.repository.*;
import com.wedding.management.domain.menu.service.DishComboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DishComboServiceImpl implements DishComboService {
    private final DishComboRepository dishComboRepository;
    private final DishComboSlotRepository dishComboSlotRepository;
    private final DishTypeRepository dishTypeRepository;
    private final DishRepository dishRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${app.menu.max-combo-discount-rate:50}")
    private Double maxComboDiscountRate;

    public DishComboServiceImpl(DishComboRepository dishComboRepository, DishComboSlotRepository dishComboSlotRepository, DishTypeRepository dishTypeRepository, DishRepository dishRepository, AuditLogRepository auditLogRepository) {
        this.dishComboRepository = dishComboRepository;
        this.dishComboSlotRepository = dishComboSlotRepository;
        this.dishTypeRepository = dishTypeRepository;
        this.dishRepository = dishRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public DishComboResponse createDishCombo(DishComboRequest request, String currentUserId) {
        // BR-CDC-2: Validate input
        validateComboInput(request);
        // BR-CDC-3: Check combo name uniqueness
        if (dishComboRepository.existsByNameAndIsDeletedFalse(request.getName())) throw new BadRequestException("MSG49: Tên combo món ăn đã tồn tại");
        List<Dish> slotDishes = loadAndValidateSlotDishes(request.getComboSlotList());
        // BR-CDC-4: Dish list must not exactly match existing combo
        validateComboContentNotDuplicated(null, slotDishes);
        double originalPrice = slotDishes.stream().mapToDouble(Dish::getUnitPrice).sum();
        double comboPrice = originalPrice * (100 - request.getComboDiscountRate()) / 100;
        // BR-CDC-5: Create combo
        DishCombo combo = DishCombo.builder().name(request.getName()).comboDiscountRate(request.getComboDiscountRate()).estimatedOriginalPricePerTable(originalPrice).estimatedComboPricePerTable(comboPrice).description(request.getDescription()).status(DishComboStatus.ACTIVE).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
        DishCombo saved = dishComboRepository.save(combo);
        saveComboSlots(saved, request.getComboSlotList(), currentUserId);
        saveAuditLog(currentUserId, "CREATE_DISH_COMBO", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Override
    public DishComboResponse updateDishCombo(UUID comboId, DishComboRequest request, String currentUserId, long lastModifiedAt) {
        DishCombo combo = dishComboRepository.findById(comboId).orElseThrow(() -> new ResourceNotFoundException("Combo món ăn không tồn tại"));
        if (combo.getIsDeleted()) throw new ResourceNotFoundException("Combo món ăn đã bị xóa");
        // BR-UDC-2: Validate input
        validateComboInput(request);
        // BR-UDC-3: Name uniqueness excluding current combo
        if (!combo.getName().equals(request.getName()) && dishComboRepository.existsByNameAndIdNotAndIsDeletedFalse(request.getName(), comboId)) throw new BadRequestException("MSG49: Tên combo món ăn đã tồn tại");
        // BR-UDC-3: Optimistic locking
        if (combo.getUpdatedAt() != null && combo.getUpdatedAt().toEpochMilli() != lastModifiedAt) throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        List<Dish> slotDishes = loadAndValidateSlotDishes(request.getComboSlotList());
        validateComboContentNotDuplicated(comboId, slotDishes);
        double originalPrice = slotDishes.stream().mapToDouble(Dish::getUnitPrice).sum();
        double comboPrice = originalPrice * (100 - request.getComboDiscountRate()) / 100;
        // BR-UDC-4: Update combo and slots
        combo.setName(request.getName()).setComboDiscountRate(request.getComboDiscountRate()).setEstimatedOriginalPricePerTable(originalPrice).setEstimatedComboPricePerTable(comboPrice).setDescription(request.getDescription()).setStatus(request.getStatus() == null ? combo.getStatus() : request.getStatus()).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        DishCombo updated = dishComboRepository.save(combo);
        dishComboSlotRepository.deleteByComboId(comboId);
        saveComboSlots(updated, request.getComboSlotList(), currentUserId);
        saveAuditLog(currentUserId, "UPDATE_DISH_COMBO", updated.getId(), updated.getName());
        return mapToResponse(updated);
    }

    @Override @Transactional(readOnly = true)
    public List<DishComboResponse> searchDishCombos(String comboName, UUID dishTypeId, String dishName, Double discountFrom, Double discountTo, Boolean isReplaceable, DishComboStatus status) {
        List<DishCombo> result = dishComboRepository.findAllActive();
        if (comboName != null && !comboName.isBlank()) result = result.stream().filter(c -> c.getName().toLowerCase().contains(comboName.toLowerCase())).collect(Collectors.toList());
        if (discountFrom != null) result = result.stream().filter(c -> c.getComboDiscountRate() >= discountFrom).collect(Collectors.toList());
        if (discountTo != null) result = result.stream().filter(c -> c.getComboDiscountRate() <= discountTo).collect(Collectors.toList());
        if (status != null) result = result.stream().filter(c -> c.getStatus() == status).collect(Collectors.toList());
        if (dishTypeId != null) result = result.stream().filter(c -> dishComboSlotRepository.findByComboId(c.getId()).stream().anyMatch(s -> s.getDishType().getId().equals(dishTypeId))).collect(Collectors.toList());
        if (dishName != null && !dishName.isBlank()) result = result.stream().filter(c -> dishComboSlotRepository.findByComboId(c.getId()).stream().anyMatch(s -> s.getDefaultDish().getName().toLowerCase().contains(dishName.toLowerCase()))).collect(Collectors.toList());
        if (isReplaceable != null) result = result.stream().filter(c -> dishComboSlotRepository.findByComboId(c.getId()).stream().anyMatch(s -> Objects.equals(s.getIsReplaceable(), isReplaceable))).collect(Collectors.toList());
        return result.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteDishCombo(UUID comboId, String currentUserId, boolean deactivateIfInUse) {
        DishCombo combo = dishComboRepository.findById(comboId).orElseThrow(() -> new ResourceNotFoundException("Combo món ăn không tồn tại"));
        if (combo.getIsDeleted()) throw new ResourceNotFoundException("Combo món ăn đã bị xóa");
        long activePackageCount = countActivePackageByCombo(comboId);
        // BR-DDC-3 Case 1: count = 0 -> soft delete
        if (activePackageCount == 0) {
            combo.setIsDeleted(true);
            combo.setUpdatedBy(currentUserId);
            combo.setUpdatedAt(Instant.now());
            dishComboRepository.save(combo);
            saveAuditLog(currentUserId, "DELETE_COMBO", comboId, combo.getName());
            return;
        }
        // BR-DDC-3 Case 2: count > 0 -> require explicit deactivation confirmation
        if (!deactivateIfInUse) throw new BadRequestException("MSG: This dishes combo is currently assigned to wedding package. You cannot delete it. Do you want to deactivate this dishes combo instead?");
        combo.setStatus(DishComboStatus.INACTIVE).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        dishComboRepository.save(combo);
        saveAuditLog(currentUserId, "DEACTIVATE_DISH_COMBO", comboId, combo.getName());
    }

    @Override @Transactional(readOnly = true)
    public List<DishComboResponse> getAllDishCombos() { return dishComboRepository.findAllActive().stream().map(this::mapToResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public DishComboResponse getDishComboById(UUID comboId) { return mapToResponse(dishComboRepository.findByIdAndIsDeletedFalse(comboId).orElseThrow(() -> new ResourceNotFoundException("Combo món ăn không tồn tại"))); }

    private void validateComboInput(DishComboRequest request) {
        if (request.getName() == null || request.getName().isBlank()) throw new BadRequestException("MSG2: Tên combo món ăn không được để trống");
        if (request.getComboDiscountRate() == null) throw new BadRequestException("MSG2: Tỷ lệ giảm giá không được để trống");
        if (request.getComboDiscountRate() <= 0) throw new BadRequestException("MSG13: Tỷ lệ giảm giá phải lớn hơn 0");
        if (request.getComboDiscountRate() >= maxComboDiscountRate) throw new BadRequestException("MSG74: Tỷ lệ giảm giá combo vượt quá cấu hình cho phép");
        if (request.getComboSlotList() == null || request.getComboSlotList().isEmpty()) throw new BadRequestException("MSG2: Danh sách slot combo không được để trống");
        long distinctDishCount = request.getComboSlotList().stream().map(DishComboSlotRequest::getDefaultDishId).filter(Objects::nonNull).distinct().count();
        if (distinctDishCount < 2) throw new BadRequestException("MSG47: Combo phải có ít nhất 2 món ăn");
        if (distinctDishCount != request.getComboSlotList().size()) throw new BadRequestException("MSG49: Combo không được chứa món ăn trùng lặp");
    }

    private List<Dish> loadAndValidateSlotDishes(List<DishComboSlotRequest> slotRequests) {
        List<Dish> dishes = new ArrayList<>();
        for (DishComboSlotRequest slot : slotRequests) {
            if (slot.getDishTypeId() == null || slot.getDefaultDishId() == null) throw new BadRequestException("MSG2: Loại món ăn và món mặc định trong slot không được để trống");
            DishType dishType = dishTypeRepository.findByIdAndIsDeletedFalse(slot.getDishTypeId()).orElseThrow(() -> new ResourceNotFoundException("Loại món ăn trong slot không tồn tại"));
            if (dishType.getStatus() != DishTypeStatus.ACTIVE) throw new BadRequestException("MSG2: Loại món ăn trong slot không còn hoạt động");
            Dish dish = dishRepository.findByIdAndIsDeletedFalse(slot.getDefaultDishId()).orElseThrow(() -> new ResourceNotFoundException("Món ăn trong slot không tồn tại"));
            if (dish.getStatus() != DishStatus.ACTIVE) throw new BadRequestException("MSG2: Món ăn trong slot không còn hoạt động");
            if (!dish.getDishType().getId().equals(dishType.getId())) throw new BadRequestException("MSG2: Món ăn không thuộc loại món ăn đã chọn trong slot");
            dishes.add(dish);
        }
        return dishes;
    }

    private void validateComboContentNotDuplicated(UUID currentComboId, List<Dish> slotDishes) {
        Set<UUID> newSet = slotDishes.stream().map(Dish::getId).collect(Collectors.toSet());
        for (DishCombo existing : dishComboRepository.findAllActive()) {
            if (currentComboId != null && existing.getId().equals(currentComboId)) continue;
            Set<UUID> existingSet = dishComboSlotRepository.findByComboId(existing.getId()).stream().map(s -> s.getDefaultDish().getId()).collect(Collectors.toSet());
            if (existingSet.equals(newSet)) throw new BadRequestException("MSG73: Danh sách món ăn đã trùng hoàn toàn với một combo hiện có");
        }
    }

    private void saveComboSlots(DishCombo combo, List<DishComboSlotRequest> slotRequests, String currentUserId) {
        int order = 1;
        for (DishComboSlotRequest slot : slotRequests) {
            DishType dishType = dishTypeRepository.findByIdAndIsDeletedFalse(slot.getDishTypeId()).orElseThrow(() -> new ResourceNotFoundException("Loại món ăn trong slot không tồn tại"));
            Dish dish = dishRepository.findByIdAndIsDeletedFalse(slot.getDefaultDishId()).orElseThrow(() -> new ResourceNotFoundException("Món ăn trong slot không tồn tại"));
            DishComboSlot entity = DishComboSlot.builder().dishCombo(combo).dishType(dishType).defaultDish(dish).isReplaceable(slot.getIsReplaceable() != null && slot.getIsReplaceable()).displayOrder(slot.getDisplayOrder() == null ? order : slot.getDisplayOrder()).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
            dishComboSlotRepository.save(entity);
            order++;
        }
    }

    private long countActivePackageByCombo(UUID comboId) {
        // Replace this with WeddingPackageRepository.countActivePackageByCombo(comboId) after the Wedding Package module is implemented.
        return 0;
    }

    private DishComboResponse mapToResponse(DishCombo combo) {
        List<DishComboSlot> slots = dishComboSlotRepository.findByComboId(combo.getId());
        List<DishComboSlotResponse> slotResponses = slots.stream().map(s -> DishComboSlotResponse.builder().id(s.getId()).dishTypeId(s.getDishType().getId()).dishTypeName(s.getDishType().getName()).defaultDishId(s.getDefaultDish().getId()).defaultDishName(s.getDefaultDish().getName()).isReplaceable(s.getIsReplaceable()).displayOrder(s.getDisplayOrder()).build()).collect(Collectors.toList());
        String summary = slots.stream().map(s -> s.getDishType().getName() + ": " + s.getDefaultDish().getName()).collect(Collectors.joining("\n"));
        int replaceableCount = (int) slots.stream().filter(s -> Boolean.TRUE.equals(s.getIsReplaceable())).count();
        return DishComboResponse.builder().id(combo.getId()).name(combo.getName()).comboDiscountRate(combo.getComboDiscountRate()).estimatedOriginalPricePerTable(combo.getEstimatedOriginalPricePerTable()).estimatedComboPricePerTable(combo.getEstimatedComboPricePerTable()).description(combo.getDescription()).status(combo.getStatus()).slots(slotResponses).slotSummary(summary).numberOfSlots(slots.size()).replaceableSlotCount(replaceableCount).lastModifiedAt(combo.getUpdatedAt()).lastModifiedBy(combo.getUpdatedBy()).build();
    }
    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) { try { auditLogRepository.save(AuditLog.builder().userId(UUID.fromString(userId)).action(action).targetId(targetId).targetName(targetName).createdAt(Instant.now()).build()); } catch (IllegalArgumentException ignored) {} }
}
