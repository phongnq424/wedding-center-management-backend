package com.wedding.management.domain.hall.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class HallResponse {
    private UUID id;
    private String name;
    private Integer capacity;
    private Double basePrice;
    private String status;
    private String description;
}