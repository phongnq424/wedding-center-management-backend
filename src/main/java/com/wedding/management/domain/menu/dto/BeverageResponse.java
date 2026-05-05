package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.BeverageStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class BeverageResponse {
    private UUID id;
    private String name;
    private UUID beverageTypeId;
    private String beverageTypeName;
    private Double unitPrice;
    private String beverageImage;
    private String description;
    private BeverageStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
