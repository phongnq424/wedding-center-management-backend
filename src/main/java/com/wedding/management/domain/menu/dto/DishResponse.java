package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.DishStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class DishResponse {
    private UUID id;
    private String name;
    private UUID dishTypeId;
    private String dishTypeName;
    private Double unitPrice;
    private String dishImage;
    private String description;
    private DishStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
