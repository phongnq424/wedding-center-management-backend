package com.wedding.management.domain.weddingpackage.repository;

import com.wedding.management.domain.weddingpackage.model.WeddingPackageCondition;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface WeddingPackageConditionRepository extends JpaRepository<WeddingPackageCondition, UUID> {
    @Query("SELECT c FROM WeddingPackageCondition c WHERE c.weddingPackage.id = :packageId ORDER BY c.displayOrder ASC")
    List<WeddingPackageCondition> findByPackageId(UUID packageId);

    @Modifying
    @Query("DELETE FROM WeddingPackageCondition c WHERE c.weddingPackage.id = :packageId")
    void deleteByPackageId(UUID packageId);
}
