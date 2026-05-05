package com.wedding.management.domain.service.repository;

import com.wedding.management.domain.service.enums.ServiceStatus;
import com.wedding.management.domain.service.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findByIdAndIsDeletedFalse(UUID id);
    Optional<Service> findByNameAndIsDeletedFalse(String name);
    boolean existsByNameAndIsDeletedFalse(String name);

    @Query("SELECT s FROM Service s WHERE s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Service> findAllActive();

    @Query("SELECT s FROM Service s WHERE s.status = :status AND s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Service> findByStatus(ServiceStatus status);

    @Query("SELECT s FROM Service s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND s.isDeleted = false ORDER BY s.updatedAt DESC")
    List<Service> searchByName(String keyword);

    @Query("SELECT COUNT(wps) FROM WeddingPackageServiceItem wps WHERE wps.serviceId = :serviceId")
    long countPackageByService(UUID serviceId);
}
