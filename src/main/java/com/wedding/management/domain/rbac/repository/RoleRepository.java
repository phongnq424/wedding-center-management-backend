package com.wedding.management.domain.rbac.repository;

import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.isDeleted = false ORDER BY r.updatedAt DESC")
    List<Role> findAllActive();

    @Query("SELECT r FROM Role r WHERE r.status = :status AND r.isDeleted = false ORDER BY r.updatedAt DESC")
    List<Role> findByStatus(RoleStatus status);

    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND r.isDeleted = false ORDER BY r.updatedAt DESC")
    List<Role> searchByName(String keyword);

    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND r.status = :status AND r.isDeleted = false ORDER BY r.updatedAt DESC")
    List<Role> searchByNameAndStatus(String keyword, RoleStatus status);

    @Query("""
       SELECT COUNT(s) FROM Staff s
       WHERE s.roleId = :roleId
       AND s.isDeleted = false
       AND s.status = com.wedding.management.domain.staff.enums.StaffStatus.ACTIVE
       """)
    long countActiveStaffByRole(UUID roleId);
}
