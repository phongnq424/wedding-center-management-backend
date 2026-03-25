package com.wedding.management.domain.hall.repository;

import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.hall.enums.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HallRepository extends JpaRepository<Hall, Long>, JpaSpecificationExecutor<Hall> {
    Optional<Hall> findByIdAndIsDeletedFalse(UUID id);
    boolean existsByNameAndIsDeletedFalse(String name);
}