package com.wedding.management.domain.weddingpackage.service;

import com.wedding.management.domain.weddingpackage.dto.WeddingPackageRequest;
import com.wedding.management.domain.weddingpackage.dto.WeddingPackageResponse;
import com.wedding.management.domain.weddingpackage.enums.WeddingPackageStatus;
import java.util.*;

public interface WeddingPackageService {
    // UC66: Add Wedding Package
    WeddingPackageResponse createWeddingPackage(WeddingPackageRequest request, String currentUserId);

    // UC67: Update Wedding Package
    WeddingPackageResponse updateWeddingPackage(UUID packageId, WeddingPackageRequest request, String currentUserId, long lastModifiedAt);

    // UC68: Search Wedding Package
    List<WeddingPackageResponse> searchWeddingPackages(String packageName, List<UUID> selectedDishComboIds, List<UUID> selectedServiceIds, List<UUID> selectedBeverageIds, UUID hallTypeId, UUID shiftId, WeddingPackageStatus status);

    // UC69: Delete Wedding Package
    void deleteWeddingPackage(UUID packageId, String currentUserId, boolean deactivateIfInUse);

    // Helper methods
    List<WeddingPackageResponse> getAllWeddingPackages();
    WeddingPackageResponse getWeddingPackageById(UUID packageId);
}
