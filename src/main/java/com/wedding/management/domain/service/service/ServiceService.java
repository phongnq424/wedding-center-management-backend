package com.wedding.management.domain.service.service;

import com.wedding.management.domain.service.dto.ServiceRequest;
import com.wedding.management.domain.service.dto.ServiceResponse;
import com.wedding.management.domain.service.enums.ServiceStatus;
import java.util.List;
import java.util.UUID;

public interface ServiceService {
    // UC27: Add New Service
    ServiceResponse createService(ServiceRequest request, String currentUserId);

    // UC28: Update Service
    ServiceResponse updateService(UUID serviceId, ServiceRequest request, String currentUserId, long lastModifiedAt);

    // UC29: Search Service
    List<ServiceResponse> searchServices(String nameKeyword, Double minPrice, ServiceStatus status);

    // UC30: Delete Service
    void deleteService(UUID serviceId, String currentUserId, boolean deactivateIfInUse);

    // Helper methods
    List<ServiceResponse> getAllServices();
    List<ServiceResponse> getActiveServices();
    ServiceResponse getServiceById(UUID serviceId);
}
