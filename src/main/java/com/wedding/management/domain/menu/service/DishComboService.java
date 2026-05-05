package com.wedding.management.domain.menu.service;

import com.wedding.management.domain.menu.dto.*;
import com.wedding.management.domain.menu.enums.DishComboStatus;
import java.util.*;

public interface DishComboService {
    DishComboResponse createDishCombo(DishComboRequest request, String currentUserId);
    DishComboResponse updateDishCombo(UUID comboId, DishComboRequest request, String currentUserId, long lastModifiedAt);
    List<DishComboResponse> searchDishCombos(String comboName, UUID dishTypeId, String dishName, Double discountFrom, Double discountTo, Boolean isReplaceable, DishComboStatus status);
    void deleteDishCombo(UUID comboId, String currentUserId, boolean deactivateIfInUse);
    List<DishComboResponse> getAllDishCombos();
    DishComboResponse getDishComboById(UUID comboId);
}
