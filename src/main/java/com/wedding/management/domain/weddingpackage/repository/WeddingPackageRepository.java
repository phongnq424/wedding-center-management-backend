package com.wedding.management.domain.weddingpackage.repository;

import com.wedding.management.domain.weddingpackage.enums.WeddingPackageStatus;
import com.wedding.management.domain.weddingpackage.model.WeddingPackage;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface WeddingPackageRepository extends JpaRepository<WeddingPackage, UUID> {
    Optional<WeddingPackage> findByIdAndIsDeletedFalse(UUID id);
    Optional<WeddingPackage> findByName(String name);
    boolean existsByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("SELECT wp FROM WeddingPackage wp WHERE wp.isDeleted = false ORDER BY wp.updatedAt DESC")
    List<WeddingPackage> findAllActive();

    @Query("SELECT wp FROM WeddingPackage wp WHERE wp.status = :status AND wp.isDeleted = false ORDER BY wp.updatedAt DESC")
    List<WeddingPackage> findByStatus(WeddingPackageStatus status);
}
