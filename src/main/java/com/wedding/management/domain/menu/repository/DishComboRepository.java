package com.wedding.management.domain.menu.repository;

import com.wedding.management.domain.menu.model.DishCombo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DishComboRepository extends JpaRepository<DishCombo, UUID> {
    Optional<DishCombo> findByIdAndIsDeletedFalse(UUID id);
    Optional<DishCombo> findByName(String name);
    boolean existsByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("SELECT dc FROM DishCombo dc WHERE dc.isDeleted = false ORDER BY dc.updatedAt DESC")
    List<DishCombo> findAllActive();

    @Query("SELECT DISTINCT dc FROM DishCombo dc JOIN dc.slots s WHERE s.defaultDish.id = :dishId AND dc.isDeleted = false")
    List<DishCombo> findActiveCombosByDish(UUID dishId);

    @Query("SELECT COUNT(DISTINCT dc) FROM DishCombo dc JOIN dc.slots s WHERE s.defaultDish.id = :dishId AND dc.isDeleted = false")
    long countActiveComboByDish(UUID dishId);
}
