package com.wedding.management.domain.shift.repository;

import com.wedding.management.domain.shift.enums.ShiftStatus;
import com.wedding.management.domain.shift.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    Optional<Shift> findByIdAndIsDeletedFalse(UUID id);
    Optional<Shift> findByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIsDeletedFalse(String name);

    @Query("SELECT s FROM Shift s WHERE s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Shift> findAllActive();

    @Query("SELECT s FROM Shift s WHERE s.status = :status AND s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Shift> findByStatus(ShiftStatus status);

    @Query("SELECT s FROM Shift s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Shift> searchByName(String keyword);

    @Query("SELECT s FROM Shift s WHERE s.isDeleted = false AND (:excludeId IS NULL OR s.id <> :excludeId) " +
           "AND (:statusOnlyActive = false OR s.status = com.wedding.management.domain.shift.enums.ShiftStatus.ACTIVE) " +
           "AND (:newStart < s.endTime AND :newEnd > s.startTime)")
    List<Shift> findOverlappingShifts(LocalTime newStart, LocalTime newEnd, UUID excludeId, boolean statusOnlyActive);
}
