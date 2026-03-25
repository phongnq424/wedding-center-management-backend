package com.wedding.management.domain.service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class ServiceResponse {
    private UUID id;
    private String name;
    private Double price;
    private String status;
    private String description;
}