package com.wedding.management.domain.menu.repository;

import com.wedding.management.domain.menu.enums.DishStatus;
import com.wedding.management.domain.menu.model.Dish;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {
    Optional<Dish> findByIdAndIsDeletedFalse(UUID id);
    Optional<Dish> findByName(String name);
    boolean existsByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("SELECT d FROM Dish d WHERE d.isDeleted = false ORDER BY d.updatedAt DESC")
    List<Dish> findAllActive();

    @Query("SELECT d FROM Dish d WHERE d.status = com.wedding.management.domain.menu.enums.DishStatus.ACTIVE AND d.isDeleted = false ORDER BY d.name ASC")
    List<Dish> findAvailableForSelection();

    @Query("SELECT d FROM Dish d WHERE d.dishType.id = :dishTypeId AND d.status = com.wedding.management.domain.menu.enums.DishStatus.ACTIVE AND d.isDeleted = false ORDER BY d.name ASC")
    List<Dish> findActiveByDishTypeId(UUID dishTypeId);

    @Query("SELECT COUNT(d) FROM Dish d WHERE d.dishType.id = :dishTypeId AND d.isDeleted = false")
    long countActiveDishByDishType(UUID dishTypeId);
}
