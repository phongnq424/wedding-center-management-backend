package com.wedding.management.domain.staff.repository;

import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {

    Optional<Staff> findByIdAndIsDeletedFalse(UUID id);

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findByPhoneNumber(String phoneNumber);

    boolean existsByEmailAndIsDeletedFalse(String email);

    boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);

    @Query("SELECT s FROM Staff s WHERE s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Staff> findAllActive();

    @Query("SELECT s FROM Staff s WHERE s.status = :status AND s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Staff> findByStatus(StaffStatus status);

    @Query("""
           SELECT s FROM Staff s
           WHERE s.isDeleted = false
           AND LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           ORDER BY s.updatedAt DESC
           """)
    List<Staff> searchByFullName(String keyword);

    @Query("""
           SELECT s FROM Staff s
           WHERE s.isDeleted = false
           AND LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           ORDER BY s.updatedAt DESC
           """)
    List<Staff> searchByEmail(String keyword);

    @Query("""
           SELECT s FROM Staff s
           WHERE s.isDeleted = false
           AND s.phoneNumber LIKE CONCAT('%', :keyword, '%')
           ORDER BY s.updatedAt DESC
           """)
    List<Staff> searchByPhoneNumber(String keyword);

    @Query("""
           SELECT COUNT(s) > 0 FROM Staff s
           WHERE s.email = :email
           AND s.id <> :staffId
           AND s.isDeleted = false
           """)
    boolean existsByEmailExcludingId(String email, UUID staffId);

    @Query("""
           SELECT COUNT(s) > 0 FROM Staff s
           WHERE s.phoneNumber = :phoneNumber
           AND s.id <> :staffId
           AND s.isDeleted = false
           """)
    boolean existsByPhoneNumberExcludingId(String phoneNumber, UUID staffId);
}