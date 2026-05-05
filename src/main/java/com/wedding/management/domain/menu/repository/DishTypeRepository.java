package com.wedding.management.domain.menu.repository;

import com.wedding.management.domain.menu.enums.DishTypeStatus;
import com.wedding.management.domain.menu.model.DishType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DishTypeRepository extends JpaRepository<DishType, UUID> {
    Optional<DishType> findByName(String name);
    Optional<DishType> findByIdAndIsDeletedFalse(UUID id);
    boolean existsByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("SELECT dt FROM DishType dt WHERE dt.isDeleted = false ORDER BY dt.updatedAt DESC")
    List<DishType> findAllActive();

    @Query("SELECT dt FROM DishType dt WHERE dt.status = :status AND dt.isDeleted = false ORDER BY dt.updatedAt DESC")
    List<DishType> findByStatus(DishTypeStatus status);

    @Query("SELECT dt FROM DishType dt WHERE LOWER(dt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND dt.isDeleted = false ORDER BY dt.updatedAt DESC")
    List<DishType> searchByName(String keyword);

    @Query("SELECT dt FROM DishType dt WHERE LOWER(dt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND dt.status = :status AND dt.isDeleted = false ORDER BY dt.updatedAt DESC")
    List<DishType> searchByNameAndStatus(String keyword, DishTypeStatus status);
}
