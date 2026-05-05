package com.wedding.management.domain.service.dto;

import com.wedding.management.domain.service.enums.ServiceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class ServiceResponse {
    private UUID id;
    private String name;
    private Double price;
    private String serviceImage;
    private String description;
    private ServiceStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
