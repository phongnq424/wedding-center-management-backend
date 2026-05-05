package com.wedding.management.domain.weddingpackage.repository;

import com.wedding.management.domain.weddingpackage.model.WeddingPackageMenuCombo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface WeddingPackageMenuComboRepository extends JpaRepository<WeddingPackageMenuCombo, UUID> {
    @Query("SELECT m FROM WeddingPackageMenuCombo m WHERE m.weddingPackage.id = :packageId")
    List<WeddingPackageMenuCombo> findByPackageId(UUID packageId);

    @Modifying
    @Query("DELETE FROM WeddingPackageMenuCombo m WHERE m.weddingPackage.id = :packageId")
    void deleteByPackageId(UUID packageId);

    @Query("SELECT COUNT(m) FROM WeddingPackageMenuCombo m WHERE m.dishCombo.id = :comboId AND m.weddingPackage.isDeleted = false")
    long countActivePackageByCombo(UUID comboId);
}
