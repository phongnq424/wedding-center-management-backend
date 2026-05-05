package com.wedding.management.domain.menu.service;

import com.wedding.management.domain.menu.dto.*;
import com.wedding.management.domain.menu.enums.DishTypeStatus;
import java.util.*;

public interface DishTypeService {
    DishTypeResponse createDishType(DishTypeRequest request, String currentUserId);
    DishTypeResponse updateDishType(UUID typeId, DishTypeRequest request, String currentUserId, long lastModifiedAt);
    List<DishTypeResponse> searchDishTypes(String nameKeyword, DishTypeStatus status);
    void deleteDishType(UUID typeId, String currentUserId, boolean deactivateIfInUse);
    List<DishTypeResponse> getAllDishTypes();
    List<DishTypeResponse> getActiveDishTypes();
    DishTypeResponse getDishTypeById(UUID typeId);
}
