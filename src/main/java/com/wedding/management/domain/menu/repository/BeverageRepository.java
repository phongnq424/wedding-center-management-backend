package com.wedding.management.domain.menu.repository;

import com.wedding.management.domain.menu.model.Beverage;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface BeverageRepository extends JpaRepository<Beverage, UUID> {
    Optional<Beverage> findByIdAndIsDeletedFalse(UUID id);
    Optional<Beverage> findByName(String name);
    boolean existsByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("SELECT b FROM Beverage b WHERE b.isDeleted = false ORDER BY b.updatedAt DESC")
    List<Beverage> findAllActive();

    @Query("SELECT b FROM Beverage b WHERE b.status = com.wedding.management.domain.menu.enums.BeverageStatus.ACTIVE AND b.isDeleted = false ORDER BY b.name ASC")
    List<Beverage> findAvailableForSelection();

    @Query("SELECT COUNT(b) FROM Beverage b WHERE b.beverageType.id = :beverageTypeId AND b.isDeleted = false")
    long countActiveBeverageByBeverageType(UUID beverageTypeId);
}
