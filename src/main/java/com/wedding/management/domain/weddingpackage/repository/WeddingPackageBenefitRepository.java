package com.wedding.management.domain.weddingpackage.repository;

import com.wedding.management.domain.weddingpackage.model.WeddingPackageBenefit;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface WeddingPackageBenefitRepository extends JpaRepository<WeddingPackageBenefit, UUID> {
    @Query("SELECT b FROM WeddingPackageBenefit b WHERE b.weddingPackage.id = :packageId ORDER BY b.displayOrder ASC")
    List<WeddingPackageBenefit> findByPackageId(UUID packageId);

    @Modifying
    @Query("DELETE FROM WeddingPackageBenefit b WHERE b.weddingPackage.id = :packageId")
    void deleteByPackageId(UUID packageId);
}
