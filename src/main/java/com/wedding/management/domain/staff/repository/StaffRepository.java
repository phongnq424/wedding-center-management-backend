package com.wedding.management.domain.staff.repository;

import com.wedding.management.domain.staff.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID>, JpaSpecificationExecutor<Staff> {
    Optional<Staff> findByEmailAndIsDeletedFalse(String email);
    Optional<Staff> findByIdAndIsDeletedFalse(UUID id);
    boolean existsByEmailAndIsDeletedFalse(String email);
}