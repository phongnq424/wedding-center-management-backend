package com.wedding.management.domain.hall.repository;

import com.wedding.management.domain.hall.model.HallType;
import com.wedding.management.domain.hall.enums.HallTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HallTypeRepository extends JpaRepository<HallType, UUID> {
    Optional<HallType> findByName(String name);

    @Query("SELECT ht FROM HallType ht WHERE ht.isDeleted = false ORDER BY ht.updatedAt DESC")
    List<HallType> findAllActive();

    @Query("SELECT ht FROM HallType ht WHERE ht.status = :status AND ht.isDeleted = false ORDER BY ht.updatedAt DESC")
    List<HallType> findByStatus(HallTypeStatus status);

    @Query("SELECT ht FROM HallType ht WHERE LOWER(ht.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND ht.isDeleted = false ORDER BY ht.updatedAt DESC")
    List<HallType> searchByName(String keyword);

    @Query("SELECT ht FROM HallType ht WHERE LOWER(ht.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND ht.status = :status AND ht.isDeleted = false ORDER BY ht.updatedAt DESC")
    List<HallType> searchByNameAndStatus(String keyword, HallTypeStatus status);

    @Query("SELECT ht FROM HallType ht WHERE LOWER(ht.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND ht.basePrice >= :minBasePrice AND ht.isDeleted = false ORDER BY ht.updatedAt DESC")
    List<HallType> searchByNameAndMinPrice(String keyword, Double minBasePrice);

    @Query("SELECT COUNT(h) FROM Hall h WHERE h.hallType.id = :hallTypeId")
    long countActiveHallByHallType(UUID hallTypeId);
}
