package com.wedding.management.domain.menu.service;

import com.wedding.management.domain.menu.dto.*;
import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import java.util.*;

public interface BeverageTypeService {
    BeverageTypeResponse createBeverageType(BeverageTypeRequest request, String currentUserId);
    BeverageTypeResponse updateBeverageType(UUID typeId, BeverageTypeRequest request, String currentUserId, long lastModifiedAt);
    List<BeverageTypeResponse> searchBeverageTypes(String nameKeyword, BeverageTypeStatus status);
    void deleteBeverageType(UUID typeId, String currentUserId, boolean deactivateIfInUse);
    List<BeverageTypeResponse> getAllBeverageTypes();
    List<BeverageTypeResponse> getActiveBeverageTypes();
    BeverageTypeResponse getBeverageTypeById(UUID typeId);
}
