package com.wedding.management.domain.weddingpackage.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.hall.enums.HallTypeStatus;
import com.wedding.management.domain.hall.model.HallType;
import com.wedding.management.domain.hall.repository.HallTypeRepository;
import com.wedding.management.domain.menu.enums.*;
import com.wedding.management.domain.menu.model.*;
import com.wedding.management.domain.menu.repository.*;
import com.wedding.management.domain.service.enums.ServiceStatus;
import com.wedding.management.domain.weddingpackage.dto.*;
import com.wedding.management.domain.weddingpackage.enums.*;
import com.wedding.management.domain.weddingpackage.model.*;
import com.wedding.management.domain.weddingpackage.repository.*;
import com.wedding.management.domain.weddingpackage.service.WeddingPackageService;
import org.springframework.stereotype.Service;
import com.wedding.management.domain.service.repository.ServiceRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WeddingPackageServiceImpl implements WeddingPackageService {
    private final WeddingPackageRepository weddingPackageRepository;
    private final WeddingPackageMenuComboRepository menuComboRepository;
    private final WeddingPackageServiceItemRepository serviceItemRepository;
    private final WeddingPackageBeverageAllowanceRepository beverageAllowanceRepository;
    private final WeddingPackageBenefitRepository benefitRepository;
    private final WeddingPackageConditionRepository conditionRepository;
    private final DishComboRepository dishComboRepository;
    private final BeverageRepository beverageRepository;
    private final HallTypeRepository hallTypeRepository;
    private final AuditLogRepository auditLogRepository;
    private final ServiceRepository serviceRepository;

    public WeddingPackageServiceImpl(WeddingPackageRepository weddingPackageRepository,
                                     WeddingPackageMenuComboRepository menuComboRepository,
                                     WeddingPackageServiceItemRepository serviceItemRepository,
                                     WeddingPackageBeverageAllowanceRepository beverageAllowanceRepository,
                                     WeddingPackageBenefitRepository benefitRepository,
                                     WeddingPackageConditionRepository conditionRepository,
                                     DishComboRepository dishComboRepository,
                                     BeverageRepository beverageRepository,
                                     HallTypeRepository hallTypeRepository,
                                     ServiceRepository serviceRepository,
                                     AuditLogRepository auditLogRepository) {
        this.weddingPackageRepository = weddingPackageRepository;
        this.menuComboRepository = menuComboRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.beverageAllowanceRepository = beverageAllowanceRepository;
        this.benefitRepository = benefitRepository;
        this.conditionRepository = conditionRepository;
        this.dishComboRepository = dishComboRepository;
        this.beverageRepository = beverageRepository;
        this.hallTypeRepository = hallTypeRepository;
        this.serviceRepository = serviceRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public WeddingPackageResponse createWeddingPackage(WeddingPackageRequest request, String currentUserId) {
        // BR-CWP-2: Validate wedding package input
        validateWeddingPackageInput(request);

        // BR-CWP-3: Check package name uniqueness
        if (weddingPackageRepository.existsByNameAndIsDeletedFalse(request.getPackageName())) {
            throw new BadRequestException("MSG49: Tên gói tiệc đã tồn tại");
        }

        DishCombo defaultCombo = loadAndValidateDefaultMenuCombo(request.getDefaultMenuComboId(), request.getMenuComboOptions());

        // BR-CWP-4: Create wedding package
        WeddingPackage weddingPackage = WeddingPackage.builder()
                .name(request.getPackageName())
                .description(request.getDescription())
                .defaultMenuCombo(defaultCombo)
                .status(WeddingPackageStatus.ACTIVE)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        WeddingPackage savedPackage = weddingPackageRepository.save(weddingPackage);

        // BR-CWP-4: Save child records in the same transaction
        savePackageMenuCombos(savedPackage, request.getMenuComboOptions(), currentUserId);
        savePackageServices(savedPackage, request.getIncludedServiceList(), currentUserId);
        savePackageBeverageAllowances(savedPackage, request.getBeverageAllowanceList(), currentUserId);
        savePackageBenefits(savedPackage, request.getPackageBenefitList(), currentUserId);
        savePackageConditions(savedPackage, request.getConditionList(), currentUserId);

        // BR-CWP-5: Save audit log
        saveAuditLog(currentUserId, "CREATE_WEDDING_PACKAGE", savedPackage.getId(), savedPackage.getName());

        return mapToResponse(savedPackage);
    }

    @Override
    public WeddingPackageResponse updateWeddingPackage(UUID packageId, WeddingPackageRequest request, String currentUserId, long lastModifiedAt) {
        WeddingPackage weddingPackage = weddingPackageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Gói tiệc không tồn tại"));

        if (weddingPackage.getIsDeleted()) {
            throw new ResourceNotFoundException("Gói tiệc đã bị xóa");
        }

        // BR-UWP-2: Validate input again before update
        validateWeddingPackageInput(request);

        // BR-UWP-3: Check package name uniqueness excluding current record
        if (!weddingPackage.getName().equals(request.getPackageName())
                && weddingPackageRepository.existsByNameAndIdNotAndIsDeletedFalse(request.getPackageName(), packageId)) {
            throw new BadRequestException("MSG49: Tên gói tiệc đã tồn tại");
        }

        // BR-UWP-3: Optimistic locking
        if (weddingPackage.getUpdatedAt() != null && weddingPackage.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        DishCombo defaultCombo = loadAndValidateDefaultMenuCombo(request.getDefaultMenuComboId(), request.getMenuComboOptions());

        // BR-UWP-4: Update parent package
        weddingPackage.setName(request.getPackageName())
                .setDescription(request.getDescription())
                .setDefaultMenuCombo(defaultCombo)
                .setStatus(request.getStatus() == null ? weddingPackage.getStatus() : request.getStatus())
                .setUpdatedBy(currentUserId)
                .setUpdatedAt(Instant.now());

        WeddingPackage updatedPackage = weddingPackageRepository.save(weddingPackage);

        // BR-UWP-4: Replace child records in one transaction
        menuComboRepository.deleteByPackageId(packageId);
        serviceItemRepository.deleteByPackageId(packageId);
        beverageAllowanceRepository.deleteByPackageId(packageId);
        benefitRepository.deleteByPackageId(packageId);
        conditionRepository.deleteByPackageId(packageId);

        savePackageMenuCombos(updatedPackage, request.getMenuComboOptions(), currentUserId);
        savePackageServices(updatedPackage, request.getIncludedServiceList(), currentUserId);
        savePackageBeverageAllowances(updatedPackage, request.getBeverageAllowanceList(), currentUserId);
        savePackageBenefits(updatedPackage, request.getPackageBenefitList(), currentUserId);
        savePackageConditions(updatedPackage, request.getConditionList(), currentUserId);

        // BR-UWP-5: Save audit log
        saveAuditLog(currentUserId, "UPDATE_WEDDING_PACKAGE", updatedPackage.getId(), updatedPackage.getName());

        return mapToResponse(updatedPackage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeddingPackageResponse> searchWeddingPackages(String packageName,
                                                              List<UUID> selectedDishComboIds,
                                                              List<UUID> selectedServiceIds,
                                                              List<UUID> selectedBeverageIds,
                                                              UUID hallTypeId,
                                                              UUID shiftId,
                                                              WeddingPackageStatus status) {
        // BR-SWP-03: Search with filters combined using AND logic
        List<WeddingPackage> packages = weddingPackageRepository.findAllActive();

        if (packageName != null && !packageName.isBlank()) {
            packages = packages.stream()
                    .filter(p -> p.getName().toLowerCase().contains(packageName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            packages = packages.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (selectedDishComboIds != null && !selectedDishComboIds.isEmpty()) {
            packages = packages.stream()
                    .filter(p -> packageContainsAllDishCombos(p.getId(), selectedDishComboIds))
                    .collect(Collectors.toList());
        }

        if (selectedServiceIds != null && !selectedServiceIds.isEmpty()) {
            packages = packages.stream()
                    .filter(p -> packageContainsAllServices(p.getId(), selectedServiceIds))
                    .collect(Collectors.toList());
        }

        if (selectedBeverageIds != null && !selectedBeverageIds.isEmpty()) {
            packages = packages.stream()
                    .filter(p -> packageContainsAllBeverages(p.getId(), selectedBeverageIds))
                    .collect(Collectors.toList());
        }

        if (hallTypeId != null) {
            packages = packages.stream()
                    .filter(p -> conditionRepository.findByPackageId(p.getId()).stream()
                            .anyMatch(c -> c.getHallType() != null && c.getHallType().getId().equals(hallTypeId)))
                    .collect(Collectors.toList());
        }

        if (shiftId != null) {
            packages = packages.stream()
                    .filter(p -> conditionRepository.findByPackageId(p.getId()).stream()
                            .anyMatch(c -> c.getShiftId() != null && c.getShiftId().equals(shiftId)))
                    .collect(Collectors.toList());
        }

        return packages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteWeddingPackage(UUID packageId, String currentUserId, boolean deactivateIfInUse) {
        WeddingPackage weddingPackage = weddingPackageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Gói tiệc không tồn tại"));

        if (weddingPackage.getIsDeleted()) {
            throw new ResourceNotFoundException("Gói tiệc đã bị xóa");
        }

        // BR-DWP-3: Count active bookings by wedding package
        long activeBookingCount = countActiveBookingByWeddingPackage(packageId);

        // Case 1: count = 0 -> soft delete
        if (activeBookingCount == 0) {
            weddingPackage.setIsDeleted(true);
            weddingPackage.setDeletedBy(currentUserId);
            weddingPackage.setDeletedAt(Instant.now());
            weddingPackage.setUpdatedBy(currentUserId);
            weddingPackage.setUpdatedAt(Instant.now());
            weddingPackageRepository.save(weddingPackage);
            saveAuditLog(currentUserId, "DELETE_WEDDING_PACKAGE", packageId, weddingPackage.getName());
            return;
        }

        // Case 2: count > 0 -> require explicit deactivation confirmation from UI
        if (!deactivateIfInUse) {
            throw new BadRequestException("MSG: This wedding package is currently assigned to booking. You cannot delete it. Do you want to deactivate this wedding package instead?");
        }

        weddingPackage.setStatus(WeddingPackageStatus.INACTIVE)
                .setUpdatedBy(currentUserId)
                .setUpdatedAt(Instant.now());
        weddingPackageRepository.save(weddingPackage);
        saveAuditLog(currentUserId, "DEACTIVATE_WEDDING_PACKAGE", packageId, weddingPackage.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeddingPackageResponse> getAllWeddingPackages() {
        return weddingPackageRepository.findAllActive().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WeddingPackageResponse getWeddingPackageById(UUID packageId) {
        WeddingPackage weddingPackage = weddingPackageRepository.findByIdAndIsDeletedFalse(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Gói tiệc không tồn tại"));
        return mapToResponse(weddingPackage);
    }

    private void validateWeddingPackageInput(WeddingPackageRequest request) {
        if (request.getPackageName() == null || request.getPackageName().isBlank()) {
            throw new BadRequestException("MSG2: Tên gói tiệc không được để trống");
        }

        if (request.getMenuComboOptions() == null || request.getMenuComboOptions().isEmpty()) {
            throw new BadRequestException("MSG2: Danh sách combo món ăn không được để trống");
        }

        if (request.getDefaultMenuComboId() == null) {
            throw new BadRequestException("MSG2: Combo mặc định không được để trống");
        }

        if (!request.getMenuComboOptions().contains(request.getDefaultMenuComboId())) {
            throw new BadRequestException("MSG2: Combo mặc định phải nằm trong danh sách combo đã chọn");
        }

        if (hasDuplicatedIds(request.getMenuComboOptions())) {
            throw new BadRequestException("MSG49: Danh sách combo món ăn không được trùng lặp");
        }

        if (request.getIncludedServiceList() == null || request.getIncludedServiceList().isEmpty()) {
            throw new BadRequestException("MSG2: Danh sách dịch vụ đi kèm không được để trống");
        }

        if (request.getBeverageAllowanceList() == null || request.getBeverageAllowanceList().isEmpty()) {
            throw new BadRequestException("MSG2: Danh sách đồ uống đi kèm không được để trống");
        }

        validateServiceItems(request.getIncludedServiceList());
        validateBeverageAllowances(request.getBeverageAllowanceList());
        validateBenefits(request.getPackageBenefitList());
        validateConditions(request.getConditionList());
    }

    private DishCombo loadAndValidateDefaultMenuCombo(UUID defaultMenuComboId, List<UUID> menuComboOptions) {
        DishCombo defaultCombo = null;
        for (UUID comboId : menuComboOptions) {
            DishCombo combo = dishComboRepository.findByIdAndIsDeletedFalse(comboId)
                    .orElseThrow(() -> new ResourceNotFoundException("Combo món ăn không tồn tại"));
            if (combo.getStatus() != DishComboStatus.ACTIVE) {
                throw new BadRequestException("MSG2: Combo món ăn không còn hoạt động");
            }
            if (combo.getId().equals(defaultMenuComboId)) {
                defaultCombo = combo;
            }
        }
        if (defaultCombo == null) {
            throw new BadRequestException("MSG2: Combo mặc định không hợp lệ");
        }
        return defaultCombo;
    }

    private void validateServiceItems(List<WeddingPackageServiceItemDTO> serviceItems) {
        Set<UUID> serviceIds = new HashSet<>();

        for (WeddingPackageServiceItemDTO item : serviceItems) {
            if (item.getServiceId() == null) {
                throw new BadRequestException("MSG2: Dịch vụ không được để trống");
            }

            if (!serviceIds.add(item.getServiceId())) {
                throw new BadRequestException("MSG49: Danh sách dịch vụ không được trùng lặp");
            }

            if (item.getQuantity() != null && item.getQuantity() <= 0) {
                throw new BadRequestException("MSG13: Số lượng dịch vụ phải lớn hơn 0");
            }

            com.wedding.management.domain.service.model.Service service = serviceRepository.findByIdAndIsDeletedFalse(item.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

            if (service.getStatus() != ServiceStatus.ACTIVE) {
                throw new BadRequestException("MSG2: Dịch vụ không còn hoạt động");
            }
        }
    }

    private void validateBeverageAllowances(List<WeddingPackageBeverageAllowanceDTO> allowances) {
        Set<UUID> beverageIds = new HashSet<>();
        for (WeddingPackageBeverageAllowanceDTO item : allowances) {
            if (item.getBeverageId() == null) throw new BadRequestException("MSG2: Đồ uống không được để trống");
            if (!beverageIds.add(item.getBeverageId())) throw new BadRequestException("MSG49: Danh sách đồ uống không được trùng lặp");
            if (item.getAllowanceQuantity() == null || item.getAllowanceQuantity() <= 0) throw new BadRequestException("MSG13: Số lượng đồ uống phải lớn hơn 0");
            Beverage beverage = beverageRepository.findByIdAndIsDeletedFalse(item.getBeverageId()).orElseThrow(() -> new ResourceNotFoundException("Đồ uống không tồn tại"));
            if (beverage.getStatus() != BeverageStatus.ACTIVE) throw new BadRequestException("MSG2: Đồ uống không còn hoạt động");
        }
    }

    private void validateBenefits(List<WeddingPackageBenefitDTO> benefits) {
        if (benefits == null) return;
        for (WeddingPackageBenefitDTO benefit : benefits) {
            if (benefit.getBenefitDescription() == null || benefit.getBenefitDescription().isBlank()) {
                throw new BadRequestException("MSG2: Nội dung quyền lợi gói không được để trống");
            }
        }
    }

    private void validateConditions(List<WeddingPackageConditionDTO> conditions) {
        if (conditions == null) return;
        for (WeddingPackageConditionDTO condition : conditions) {
            if (condition.getConditionType() == null) throw new BadRequestException("MSG2: Loại điều kiện gói không được để trống");
            if (condition.getConditionType() == PackageConditionType.HALL_TYPE && condition.getHallTypeId() == null) throw new BadRequestException("MSG2: Loại sảnh trong điều kiện không được để trống");
            if (condition.getConditionType() == PackageConditionType.SHIFT && condition.getShiftId() == null) throw new BadRequestException("MSG2: Ca tiệc trong điều kiện không được để trống");
            if ((condition.getConditionType() == PackageConditionType.MIN_TABLES || condition.getConditionType() == PackageConditionType.MAX_TABLES) && (condition.getNumericValue() == null || condition.getNumericValue() <= 0)) throw new BadRequestException("MSG13: Giá trị số bàn trong điều kiện phải lớn hơn 0");
        }
    }

    private boolean hasDuplicatedIds(List<UUID> ids) {
        return ids.stream().filter(Objects::nonNull).distinct().count() != ids.size();
    }

    private void savePackageMenuCombos(WeddingPackage weddingPackage, List<UUID> comboIds, String currentUserId) {
        for (UUID comboId : comboIds) {
            DishCombo combo = dishComboRepository.findByIdAndIsDeletedFalse(comboId).orElseThrow(() -> new ResourceNotFoundException("Combo món ăn không tồn tại"));
            WeddingPackageMenuCombo entity = WeddingPackageMenuCombo.builder().weddingPackage(weddingPackage).dishCombo(combo).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
            menuComboRepository.save(entity);
        }
    }

    private void savePackageServices(
            WeddingPackage weddingPackage,
            List<WeddingPackageServiceItemDTO> serviceItems,
            String currentUserId
    ) {
        for (WeddingPackageServiceItemDTO item : serviceItems) {
            com.wedding.management.domain.service.model.Service service = serviceRepository.findByIdAndIsDeletedFalse(item.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

            WeddingPackageServiceItem entity = WeddingPackageServiceItem.builder()
                    .weddingPackage(weddingPackage)
                    .serviceId(service.getId())
                    .serviceName(service.getName())
                    .quantity(item.getQuantity() == null ? 1 : item.getQuantity())
                    .note(item.getNote())
                    .createdBy(currentUserId)
                    .createdAt(Instant.now())
                    .isDeleted(false)
                    .build();

            serviceItemRepository.save(entity);
        }
    }

    private void savePackageBeverageAllowances(WeddingPackage weddingPackage, List<WeddingPackageBeverageAllowanceDTO> allowances, String currentUserId) {
        for (WeddingPackageBeverageAllowanceDTO item : allowances) {
            Beverage beverage = beverageRepository.findByIdAndIsDeletedFalse(item.getBeverageId()).orElseThrow(() -> new ResourceNotFoundException("Đồ uống không tồn tại"));
            WeddingPackageBeverageAllowance entity = WeddingPackageBeverageAllowance.builder().weddingPackage(weddingPackage).beverage(beverage).allowanceQuantity(item.getAllowanceQuantity()).note(item.getNote()).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
            beverageAllowanceRepository.save(entity);
        }
    }

    private void savePackageBenefits(WeddingPackage weddingPackage, List<WeddingPackageBenefitDTO> benefits, String currentUserId) {
        if (benefits == null) return;
        int order = 1;
        for (WeddingPackageBenefitDTO benefit : benefits) {
            WeddingPackageBenefit entity = WeddingPackageBenefit.builder().weddingPackage(weddingPackage).benefitDescription(benefit.getBenefitDescription()).displayOrder(benefit.getDisplayOrder() == null ? order : benefit.getDisplayOrder()).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
            benefitRepository.save(entity);
            order++;
        }
    }

    private void savePackageConditions(WeddingPackage weddingPackage, List<WeddingPackageConditionDTO> conditions, String currentUserId) {
        if (conditions == null) return;
        int order = 1;
        for (WeddingPackageConditionDTO condition : conditions) {
            HallType hallType = null;
            if (condition.getHallTypeId() != null) {
                hallType = hallTypeRepository.findById(condition.getHallTypeId()).orElseThrow(() -> new ResourceNotFoundException("Loại sảnh không tồn tại"));
                if (hallType.getIsDeleted() || hallType.getStatus() != HallTypeStatus.ACTIVE) throw new BadRequestException("MSG2: Loại sảnh trong điều kiện không còn hoạt động");
            }
            WeddingPackageCondition entity = WeddingPackageCondition.builder().weddingPackage(weddingPackage).conditionType(condition.getConditionType()).hallType(hallType).shiftId(condition.getShiftId()).shiftName(condition.getShiftName()).numericValue(condition.getNumericValue()).conditionValue(condition.getConditionValue()).displayOrder(condition.getDisplayOrder() == null ? order : condition.getDisplayOrder()).createdBy(currentUserId).createdAt(Instant.now()).isDeleted(false).build();
            conditionRepository.save(entity);
            order++;
        }
    }

    private boolean packageContainsAllDishCombos(UUID packageId, List<UUID> comboIds) {
        Set<UUID> existingIds = menuComboRepository.findByPackageId(packageId).stream().map(m -> m.getDishCombo().getId()).collect(Collectors.toSet());
        return existingIds.containsAll(comboIds);
    }

    private boolean packageContainsAllServices(UUID packageId, List<UUID> serviceIds) {
        Set<UUID> existingIds = serviceItemRepository.findByPackageId(packageId).stream().map(WeddingPackageServiceItem::getServiceId).collect(Collectors.toSet());
        return existingIds.containsAll(serviceIds);
    }

    private boolean packageContainsAllBeverages(UUID packageId, List<UUID> beverageIds) {
        Set<UUID> existingIds = beverageAllowanceRepository.findByPackageId(packageId).stream().map(b -> b.getBeverage().getId()).collect(Collectors.toSet());
        return existingIds.containsAll(beverageIds);
    }

    private long countActiveBookingByWeddingPackage(UUID packageId) {
        // Replace this with BookingRepository.countActiveBookingByWeddingPackage(packageId) after the Booking module is implemented.
        return 0;
    }

    private WeddingPackageResponse mapToResponse(WeddingPackage weddingPackage) {
        List<WeddingPackageMenuCombo> menuCombos = menuComboRepository.findByPackageId(weddingPackage.getId());
        List<WeddingPackageServiceItem> services = serviceItemRepository.findByPackageId(weddingPackage.getId());
        List<WeddingPackageBeverageAllowance> beverages = beverageAllowanceRepository.findByPackageId(weddingPackage.getId());
        List<WeddingPackageBenefit> benefits = benefitRepository.findByPackageId(weddingPackage.getId());
        List<WeddingPackageCondition> conditions = conditionRepository.findByPackageId(weddingPackage.getId());

        List<UUID> comboIds = menuCombos.stream().map(m -> m.getDishCombo().getId()).collect(Collectors.toList());
        List<String> comboNames = menuCombos.stream().map(m -> m.getDishCombo().getName()).collect(Collectors.toList());

        double originalMenuComboPrice = calculateOriginalComboPrice(weddingPackage.getDefaultMenuCombo());
        double discountedMenuComboPrice = calculateDiscountedComboPrice(weddingPackage.getDefaultMenuCombo());

        double includedServiceTotal = calculateIncludedServiceTotal(services);
        double beverageAllowanceTotal = calculateBeverageAllowanceTotal(beverages);

        double originalPackageTotal = originalMenuComboPrice + includedServiceTotal + beverageAllowanceTotal;
        double estimatedPackageTotal = discountedMenuComboPrice + includedServiceTotal + beverageAllowanceTotal;

        double estimatedSavingsAmount = originalPackageTotal - estimatedPackageTotal;
        double estimatedSavingsRate = calculateSavingsRate(originalPackageTotal, estimatedSavingsAmount);

        List<WeddingPackageServiceItemDTO> serviceDTOs = services.stream().map(s -> WeddingPackageServiceItemDTO.builder().id(s.getId()).serviceId(s.getServiceId()).serviceName(s.getServiceName()).quantity(s.getQuantity()).note(s.getNote()).build()).collect(Collectors.toList());
        List<WeddingPackageBeverageAllowanceDTO> beverageDTOs = beverages.stream().map(b -> WeddingPackageBeverageAllowanceDTO.builder().id(b.getId()).beverageId(b.getBeverage().getId()).beverageName(b.getBeverage().getName()).allowanceQuantity(b.getAllowanceQuantity()).note(b.getNote()).build()).collect(Collectors.toList());
        List<WeddingPackageBenefitDTO> benefitDTOs = benefits.stream().map(b -> WeddingPackageBenefitDTO.builder().id(b.getId()).benefitDescription(b.getBenefitDescription()).displayOrder(b.getDisplayOrder()).build()).collect(Collectors.toList());
        List<WeddingPackageConditionDTO> conditionDTOs = conditions.stream().map(c -> WeddingPackageConditionDTO.builder().id(c.getId()).conditionType(c.getConditionType()).hallTypeId(c.getHallType() == null ? null : c.getHallType().getId()).hallTypeName(c.getHallType() == null ? null : c.getHallType().getName()).shiftId(c.getShiftId()).shiftName(c.getShiftName()).numericValue(c.getNumericValue()).conditionValue(c.getConditionValue()).displayOrder(c.getDisplayOrder()).build()).collect(Collectors.toList());

        String menuSummary = comboNames.stream().collect(Collectors.joining("\n"));
        String serviceSummary = serviceDTOs.stream().map(s -> (s.getServiceName() == null ? String.valueOf(s.getServiceId()) : s.getServiceName()) + " x" + (s.getQuantity() == null ? 1 : s.getQuantity())).collect(Collectors.joining("\n"));
        String beverageSummary = beverageDTOs.stream().map(b -> b.getBeverageName() + " x" + b.getAllowanceQuantity()).collect(Collectors.joining("\n"));
        String conditionSummary = conditionDTOs.stream().map(c -> c.getConditionType() + ": " + (c.getHallTypeName() != null ? c.getHallTypeName() : c.getShiftName() != null ? c.getShiftName() : c.getNumericValue() != null ? c.getNumericValue().toString() : c.getConditionValue())).collect(Collectors.joining("\n"));

        return WeddingPackageResponse.builder()
                .id(weddingPackage.getId())
                .packageName(weddingPackage.getName())
                .description(weddingPackage.getDescription())
                .defaultMenuComboId(weddingPackage.getDefaultMenuCombo().getId())
                .defaultMenuComboName(weddingPackage.getDefaultMenuCombo().getName())
                .menuComboOptions(comboIds)
                .menuComboNames(comboNames)
                .includedServiceList(serviceDTOs)
                .beverageAllowanceList(beverageDTOs)
                .packageBenefitList(benefitDTOs)
                .conditionList(conditionDTOs)
                .menuComboSummary(menuSummary)
                .estimatedOriginalMenuComboPrice(originalMenuComboPrice)
                .estimatedDiscountedMenuComboPrice(discountedMenuComboPrice)
                .includedServiceTotal(includedServiceTotal)
                .beverageAllowanceTotal(beverageAllowanceTotal)
                .originalPackageTotal(originalPackageTotal)
                .estimatedPackageTotal(estimatedPackageTotal)
                .estimatedSavingsAmount(estimatedSavingsAmount)
                .estimatedSavingsRate(estimatedSavingsRate)
                .serviceSummary(serviceSummary)
                .beverageAllowanceSummary(beverageSummary)
                .conditionSummary(conditionSummary)
                .numberOfMenuCombos(menuCombos.size())
                .numberOfIncludedServices(services.size())
                .numberOfBeverageAllowances(beverages.size())
                .status(weddingPackage.getStatus())
                .lastModifiedAt(weddingPackage.getUpdatedAt())
                .lastModifiedBy(weddingPackage.getUpdatedBy())
                .build();
    }

    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) {
        try {
            UUID userUUID = UUID.fromString(userId);
            AuditLog auditLog = AuditLog.builder().userId(userUUID).action(action).targetId(targetId).targetName(targetName).createdAt(Instant.now()).build();
            auditLogRepository.save(auditLog);
        } catch (IllegalArgumentException ignored) {
            // If userId is not a valid UUID, skip audit logging to match the current project style.
        }
    }
    private double calculateOriginalComboPrice(DishCombo combo) {
        if (combo == null || combo.getSlots() == null) {
            return 0.0;
        }

        return combo.getSlots().stream()
                .filter(slot -> slot.getDefaultDish() != null)
                .mapToDouble(slot -> slot.getDefaultDish().getUnitPrice() == null ? 0.0 : slot.getDefaultDish().getUnitPrice())
                .sum();
    }

    private double calculateDiscountedComboPrice(DishCombo combo) {
        double originalPrice = calculateOriginalComboPrice(combo);

        if (combo == null || combo.getComboDiscountRate() == null) {
            return originalPrice;
        }

        double discountRate = combo.getComboDiscountRate();

        return originalPrice * (1 - discountRate / 100.0);
    }

    private double calculateIncludedServiceTotal(List<WeddingPackageServiceItem> services) {
        if (services == null || services.isEmpty()) {
            return 0.0;
        }

        return services.stream()
                .mapToDouble(item -> {
                    if (item.getServiceId() == null) {
                        return 0.0;
                    }

                    return serviceRepository.findById(item.getServiceId())
                            .map(service -> {
                                double price = service.getPrice() == null ? 0.0 : service.getPrice();
                                int quantity = item.getQuantity() == null ? 1 : item.getQuantity();
                                return price * quantity;
                            })
                            .orElse(0.0);
                })
                .sum();
    }

    private double calculateBeverageAllowanceTotal(List<WeddingPackageBeverageAllowance> beverages) {
        if (beverages == null || beverages.isEmpty()) {
            return 0.0;
        }

        return beverages.stream()
                .mapToDouble(item -> {
                    if (item.getBeverage() == null) {
                        return 0.0;
                    }

                    double price = item.getBeverage().getUnitPrice() == null ? 0.0 : item.getBeverage().getUnitPrice();
                    int quantity = item.getAllowanceQuantity() == null ? 0 : item.getAllowanceQuantity();

                    return price * quantity;
                })
                .sum();
    }

    private double calculateSavingsRate(double originalTotal, double savingsAmount) {
        if (originalTotal <= 0) {
            return 0.0;
        }

        return savingsAmount / originalTotal * 100.0;
    }
}
