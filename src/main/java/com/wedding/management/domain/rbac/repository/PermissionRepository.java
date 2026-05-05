package com.wedding.management.domain.rbac.repository;

import com.wedding.management.domain.rbac.model.Permission;
import com.wedding.management.domain.rbac.enums.PermissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByCode(String code);

    @Query("SELECT p FROM Permission p WHERE p.isDeleted = false ORDER BY p.updatedAt DESC")
    List<Permission> findAllActive();

    @Query("SELECT p FROM Permission p WHERE p.status = :status AND p.isDeleted = false ORDER BY p.updatedAt DESC")
    List<Permission> findByStatus(PermissionStatus status);

    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isDeleted = false ORDER BY p.updatedAt DESC")
    List<Permission> searchByName(String keyword);

    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.status = :status AND p.isDeleted = false ORDER BY p.updatedAt DESC")
    List<Permission> searchByNameAndStatus(String keyword, PermissionStatus status);

    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    long countActiveRoleByPermission(UUID permissionId);
}
