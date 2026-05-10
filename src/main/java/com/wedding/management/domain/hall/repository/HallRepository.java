package com.wedding.management.domain.hall.repository;

import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.hall.enums.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HallRepository extends JpaRepository<Hall, UUID>, JpaSpecificationExecutor<Hall> {
    Optional<Hall> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("SELECT h FROM Hall h WHERE h.isDeleted = false ORDER BY h.updatedAt DESC")
    List<Hall> findAllActive();

    @Query("SELECT h FROM Hall h WHERE h.status = :status AND h.isDeleted = false ORDER BY h.updatedAt DESC")
    List<Hall> findByStatus(HallStatus status);

    @Query("SELECT h FROM Hall h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND h.isDeleted = false ORDER BY h.updatedAt DESC")
    List<Hall> searchByName(String keyword);

    @Query("SELECT h FROM Hall h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND h.hallType.id = :hallTypeId AND h.isDeleted = false ORDER BY h.updatedAt DESC")
    List<Hall> searchByNameAndHallType(String keyword, UUID hallTypeId);

    @Query("SELECT h FROM Hall h WHERE h.minTables >= :minTablesFrom AND h.maxTables <= :maxTablesTo " +
           "AND h.isDeleted = false ORDER BY h.updatedAt DESC")
    List<Hall> searchByTableCapacity(Integer minTablesFrom, Integer maxTablesTo);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.hall.id = :hallId AND b.bookingDate > :currentTime")
    long countFutureBookingByHall(UUID hallId, Instant currentTime);

    @Query("SELECT h FROM Hall h WHERE h.id = :hallId AND EXISTS (SELECT b FROM Booking b WHERE b.hall.id = h.id AND CAST(b.bookingDate AS date) = CAST(:today AS date))")
    Optional<Hall> findHallInUseToday(UUID hallId, Instant today);

}
