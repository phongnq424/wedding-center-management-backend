package com.wedding.management.domain.weddingpackage.repository;

import com.wedding.management.domain.weddingpackage.model.WeddingPackageServiceItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface WeddingPackageServiceItemRepository extends JpaRepository<WeddingPackageServiceItem, UUID> {
    @Query("SELECT s FROM WeddingPackageServiceItem s WHERE s.weddingPackage.id = :packageId")
    List<WeddingPackageServiceItem> findByPackageId(UUID packageId);

    @Modifying
    @Query("DELETE FROM WeddingPackageServiceItem s WHERE s.weddingPackage.id = :packageId")
    void deleteByPackageId(UUID packageId);
}
