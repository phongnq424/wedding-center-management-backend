package com.wedding.management.domain.service.repository;

import com.wedding.management.domain.service.model.WeddingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<WeddingService, UUID>, JpaSpecificationExecutor<WeddingService> {

    Optional<WeddingService> findByIdAndIsDeletedFalse(UUID id);
    boolean existsByNameAndIsDeletedFalse(String name);
}