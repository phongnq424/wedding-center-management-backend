package com.wedding.management.domain.menu.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.config.supabase.SupabaseFileUploadService;
import com.wedding.management.domain.menu.dto.DishRequest;
import com.wedding.management.domain.menu.dto.DishResponse;
import com.wedding.management.domain.menu.enums.DishStatus;
import com.wedding.management.domain.menu.enums.DishTypeStatus;
import com.wedding.management.domain.menu.model.Dish;
import com.wedding.management.domain.menu.model.DishType;
import com.wedding.management.domain.menu.repository.DishComboRepository;
import com.wedding.management.domain.menu.repository.DishRepository;
import com.wedding.management.domain.menu.repository.DishTypeRepository;
import com.wedding.management.domain.menu.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishTypeRepository dishTypeRepository;
    private final DishComboRepository dishComboRepository;
    private final AuditLogRepository auditLogRepository;
    private final SupabaseFileUploadService fileUploadService;

    public DishServiceImpl(DishRepository dishRepository, DishTypeRepository dishTypeRepository,
            DishComboRepository dishComboRepository, AuditLogRepository auditLogRepository,
            SupabaseFileUploadService fileUploadService) {
        this.dishRepository = dishRepository;
        this.dishTypeRepository = dishTypeRepository;
        this.dishComboRepository = dishComboRepository;
        this.auditLogRepository = auditLogRepository;
        this.fileUploadService = fileUploadService;
    }

    @Override
    public DishResponse createDish(DishRequest request, String currentUserId) {
        // BR-CD-2: Validate input
        validateDishInput(request.getName(), request.getDishTypeId(), request.getUnitPrice());
        DishType dishType = dishTypeRepository.findByIdAndIsDeletedFalse(request.getDishTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại món ăn không tồn tại"));
        if (dishType.getStatus() != DishTypeStatus.ACTIVE)
            throw new BadRequestException("MSG2: Loại món ăn không còn hoạt động");
        // BR-CD-3: Check uniqueness
        if (dishRepository.existsByNameAndIsDeletedFalse(request.getName()))
            throw new BadRequestException("MSG49: Tên món ăn đã tồn tại");

        // Handle image upload
        String dishImageUrl = null;
        if (request.getDishImage() != null && !request.getDishImage().isEmpty()) {
            try {
                dishImageUrl = fileUploadService.uploadPublicFile(request.getDishImage(), "dishes");
                log.info("Dish image uploaded: {}", dishImageUrl);
            } catch (IOException e) {
                log.error("Error uploading dish image: {}", e.getMessage(), e);
                throw new BadRequestException("Lỗi upload hình ảnh: " + e.getMessage());
            }
        }

        // BR-CD-4: Create dish
        Dish dish = Dish.builder().name(request.getName()).dishType(dishType).unitPrice(request.getUnitPrice())
                .dishImage(dishImageUrl).description(request.getDescription()).status(DishStatus.ACTIVE)
                .createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
        Dish saved = dishRepository.save(dish);
        saveAuditLog(currentUserId, "CREATE_DISH", saved.getId(), saved.getName());
        return mapToResponse(saved);
    }

    @Override
    public DishResponse updateDish(UUID dishId, DishRequest request, String currentUserId, long lastModifiedAt) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));
        if (dish.getIsDeleted())
            throw new ResourceNotFoundException("Món ăn đã bị xóa");
        // BR-UD-2: Validate input
        validateDishInput(request.getName(), request.getDishTypeId(), request.getUnitPrice());
        DishType dishType = dishTypeRepository.findByIdAndIsDeletedFalse(request.getDishTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại món ăn không tồn tại"));
        if (dishType.getStatus() != DishTypeStatus.ACTIVE)
            throw new BadRequestException("MSG2: Loại món ăn không còn hoạt động");
        // BR-UD-3: Check uniqueness excluding current record
        if (!dish.getName().equals(request.getName())
                && dishRepository.existsByNameAndIdNotAndIsDeletedFalse(request.getName(), dishId))
            throw new BadRequestException("MSG49: Tên món ăn đã tồn tại");
        // BR-UD-3: Optimistic locking
        if (dish.getUpdatedAt() != null && dish.getUpdatedAt().toEpochMilli() != lastModifiedAt)
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");

        // Handle image upload and replacement
        String dishImageUrl = dish.getDishImage();
        if (request.getDishImage() != null && !request.getDishImage().isEmpty()) {
            try {
                // Delete old image if exists
                if (dish.getDishImage() != null && !dish.getDishImage().isEmpty()) {
                    try {
                        String oldPath = extractFilePath(dish.getDishImage());
                        fileUploadService.deleteFile("public-assets", oldPath);
                        log.info("Old dish image deleted: {}", oldPath);
                    } catch (Exception e) {
                        log.warn("Failed to delete old image: {}", e.getMessage());
                    }
                }

                // Upload new image
                dishImageUrl = fileUploadService.uploadPublicFile(request.getDishImage(), "dishes");
                log.info("Dish image updated: {}", dishImageUrl);
            } catch (IOException e) {
                log.error("Error uploading dish image: {}", e.getMessage(), e);
                throw new BadRequestException("Lỗi upload hình ảnh: " + e.getMessage());
            }
        }

        // BR-UD-4: Update dish; price change only affects future snapshots
        dish.setName(request.getName()).setDishType(dishType).setUnitPrice(request.getUnitPrice())
                .setDishImage(dishImageUrl).setDescription(request.getDescription())
                .setStatus(request.getStatus() == null ? dish.getStatus() : request.getStatus())
                .setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        Dish updated = dishRepository.save(dish);
        saveAuditLog(currentUserId, "UPDATE_DISH", updated.getId(), updated.getName());
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> searchDishes(String dishName, UUID dishTypeId, Double priceFrom, Double priceTo,
            DishStatus status) {
        List<Dish> result = dishRepository.findAllActive();
        if (dishName != null && !dishName.isBlank())
            result = result.stream().filter(d -> d.getName().toLowerCase().contains(dishName.toLowerCase()))
                    .collect(Collectors.toList());
        if (dishTypeId != null)
            result = result.stream().filter(d -> d.getDishType().getId().equals(dishTypeId))
                    .collect(Collectors.toList());
        if (priceFrom != null)
            result = result.stream().filter(d -> d.getUnitPrice() >= priceFrom).collect(Collectors.toList());
        if (priceTo != null)
            result = result.stream().filter(d -> d.getUnitPrice() <= priceTo).collect(Collectors.toList());
        if (status != null)
            result = result.stream().filter(d -> d.getStatus() == status).collect(Collectors.toList());
        return result.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteDish(UUID dishId, String currentUserId, boolean deactivateIfInUse) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại"));
        if (dish.getIsDeleted())
            throw new ResourceNotFoundException("Món ăn đã bị xóa");
        long comboCount = dishComboRepository.countActiveComboByDish(dishId);
        // BR-DD-3 Case 1: not used in combo -> soft delete
        if (comboCount == 0) {
            dish.setIsDeleted(true);
            dish.setUpdatedBy(currentUserId);
            dish.setUpdatedAt(Instant.now());
            dishRepository.save(dish);
            saveAuditLog(currentUserId, "DELETE_DISH", dishId, dish.getName());
            return;
        }
        // BR-DD-3 Case 2: used in combo -> require explicit deactivation confirmation
        if (!deactivateIfInUse)
            throw new BadRequestException(
                    "MSG: This dish is currently assigned to dish combo. You cannot delete it. Do you want to deactivate this dish instead?");
        dish.setStatus(DishStatus.INACTIVE).setUpdatedBy(currentUserId).setUpdatedAt(Instant.now());
        dishRepository.save(dish);
        saveAuditLog(currentUserId, "DEACTIVATE_DISH", dishId, dish.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> getAllDishes() {
        return dishRepository.findAllActive().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> getActiveDishes() {
        return dishRepository.findAvailableForSelection().stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> getActiveDishesByType(UUID dishTypeId) {
        return dishRepository.findActiveByDishTypeId(dishTypeId).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DishResponse getDishById(UUID dishId) {
        return mapToResponse(dishRepository.findByIdAndIsDeletedFalse(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn không tồn tại")));
    }

    private void validateDishInput(String name, UUID dishTypeId, Double unitPrice) {
        if (name == null || name.isBlank())
            throw new BadRequestException("MSG2: Tên món ăn không được để trống");
        if (dishTypeId == null)
            throw new BadRequestException("MSG2: Loại món ăn không được để trống");
        if (unitPrice == null)
            throw new BadRequestException("MSG2: Đơn giá không được để trống");
        if (unitPrice <= 0)
            throw new BadRequestException("MSG13: Đơn giá phải lớn hơn 0");
    }

    private String extractFilePath(String imageUrl) {
        // Extract path from URL:
        // https://xxxxx.supabase.co/storage/v1/object/public/public-assets/dishes/filename.jpg
        // Returns: dishes/filename.jpg
        if (imageUrl == null)
            return null;
        String[] parts = imageUrl.split("public-assets/");
        if (parts.length > 1) {
            return parts[1];
        }
        return imageUrl;
    }

    private DishResponse mapToResponse(Dish d) {
        return DishResponse.builder().id(d.getId()).name(d.getName()).dishTypeId(d.getDishType().getId())
                .dishTypeName(d.getDishType().getName()).unitPrice(d.getUnitPrice()).dishImage(d.getDishImage())
                .description(d.getDescription()).status(d.getStatus()).lastModifiedAt(d.getUpdatedAt())
                .lastModifiedBy(d.getUpdatedBy()).build();
    }

    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) {
        try {
            auditLogRepository.save(AuditLog.builder().userId(UUID.fromString(userId)).action(action).targetId(targetId)
                    .targetName(targetName).createdAt(Instant.now()).build());
        } catch (IllegalArgumentException ignored) {
        }
    }
}
