package com.wedding.management.domain.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class HallAvailabilityResponse {
    private UUID hallId;
    private String hallName;
    private UUID hallTypeId;
    private String hallTypeName;
    private String hallImage;
    private Double price;
    private Integer maxTables;
    private String description;
    private String status;
}
