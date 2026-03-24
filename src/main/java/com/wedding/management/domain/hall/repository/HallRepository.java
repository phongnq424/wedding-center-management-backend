package com.wedding.management.domain.hall.repository;

import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.hall.enums.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface HallRepository extends JpaRepository<Hall, Long> {
    Optional<Hall> findByIdAndIsDeletedFalse(Long id);
    boolean existsByNameAndIsDeletedFalse(String name);
    @Query("SELECT h FROM Hall h WHERE h.isDeleted = false " +
            "AND (:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:capacity IS NULL OR h.capacity >= :capacity) " +
            "AND (:status IS NULL OR h.status = :status)")
    List<Hall> searchHalls(
            @Param("name") String name,
            @Param("capacity") Integer capacity,
            @Param("status") HallStatus status
    );
}