package com.wedding.management.domain.hall.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class HallResponse {
    private Long id;
    private String name;
    private Integer capacity;
    private Double basePrice;
    private String status;
    private String description;
}