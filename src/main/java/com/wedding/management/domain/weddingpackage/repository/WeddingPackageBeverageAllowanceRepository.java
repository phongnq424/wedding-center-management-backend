package com.wedding.management.domain.weddingpackage.repository;

import com.wedding.management.domain.weddingpackage.model.WeddingPackageBeverageAllowance;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface WeddingPackageBeverageAllowanceRepository extends JpaRepository<WeddingPackageBeverageAllowance, UUID> {
    @Query("SELECT b FROM WeddingPackageBeverageAllowance b WHERE b.weddingPackage.id = :packageId")
    List<WeddingPackageBeverageAllowance> findByPackageId(UUID packageId);

    @Modifying
    @Query("DELETE FROM WeddingPackageBeverageAllowance b WHERE b.weddingPackage.id = :packageId")
    void deleteByPackageId(UUID packageId);

    @Query("SELECT COUNT(b) FROM WeddingPackageBeverageAllowance b WHERE b.beverage.id = :beverageId AND b.weddingPackage.isDeleted = false")
    long countActivePackageByBeverage(UUID beverageId);
}
