package com.wedding.management.domain.menu.repository;

import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import com.wedding.management.domain.menu.model.BeverageType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface BeverageTypeRepository extends JpaRepository<BeverageType, UUID> {
    Optional<BeverageType> findByName(String name);
    Optional<BeverageType> findByIdAndIsDeletedFalse(UUID id);
    boolean existsByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("SELECT bt FROM BeverageType bt WHERE bt.isDeleted = false ORDER BY bt.updatedAt DESC")
    List<BeverageType> findAllActive();

    @Query("SELECT bt FROM BeverageType bt WHERE bt.status = :status AND bt.isDeleted = false ORDER BY bt.updatedAt DESC")
    List<BeverageType> findByStatus(BeverageTypeStatus status);

    @Query("SELECT bt FROM BeverageType bt WHERE LOWER(bt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND bt.isDeleted = false ORDER BY bt.updatedAt DESC")
    List<BeverageType> searchByName(String keyword);

    @Query("SELECT bt FROM BeverageType bt WHERE LOWER(bt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND bt.status = :status AND bt.isDeleted = false ORDER BY bt.updatedAt DESC")
    List<BeverageType> searchByNameAndStatus(String keyword, BeverageTypeStatus status);
}
