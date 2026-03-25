package com.wedding.management.domain.service.service;

import com.wedding.management.domain.service.dto.ServiceRequest;
import com.wedding.management.domain.service.dto.ServiceResponse;
import java.util.List;
import java.util.UUID;

public interface WeddingServiceService {
    ServiceResponse createService(ServiceRequest request);

    ServiceResponse updateService(UUID id, ServiceRequest request);

    List<ServiceResponse> searchServices(String name, String category, String status);

    void deleteService(UUID id);
}