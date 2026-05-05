package com.wedding.management.domain.menu.service;

import com.wedding.management.domain.menu.dto.*;
import com.wedding.management.domain.menu.enums.DishStatus;
import java.util.*;

public interface DishService {
    DishResponse createDish(DishRequest request, String currentUserId);
    DishResponse updateDish(UUID dishId, DishRequest request, String currentUserId, long lastModifiedAt);
    List<DishResponse> searchDishes(String dishName, UUID dishTypeId, Double priceFrom, Double priceTo, DishStatus status);
    void deleteDish(UUID dishId, String currentUserId, boolean deactivateIfInUse);
    List<DishResponse> getAllDishes();
    List<DishResponse> getActiveDishes();
    List<DishResponse> getActiveDishesByType(UUID dishTypeId);
    DishResponse getDishById(UUID dishId);
}
