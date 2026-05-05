package com.wedding.management.domain.menu.service;

import com.wedding.management.domain.menu.dto.*;
import com.wedding.management.domain.menu.enums.BeverageStatus;
import java.util.*;

public interface BeverageService {
    BeverageResponse createBeverage(BeverageRequest request, String currentUserId);
    BeverageResponse updateBeverage(UUID beverageId, BeverageRequest request, String currentUserId, long lastModifiedAt);
    List<BeverageResponse> searchBeverages(String beverageName, UUID beverageTypeId, Double priceFrom, Double priceTo, BeverageStatus status);
    void deleteBeverage(UUID beverageId, String currentUserId, boolean deactivateIfInUse);
    List<BeverageResponse> getAllBeverages();
    List<BeverageResponse> getActiveBeverages();
    BeverageResponse getBeverageById(UUID beverageId);
}
