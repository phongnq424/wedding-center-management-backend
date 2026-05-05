package com.wedding.management.domain.hall.dto;

import com.wedding.management.domain.hall.enums.HallStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class HallResponse {
    private UUID id;
    private String name;
    private UUID hallTypeId;
    private String hallTypeName;
    private Double basePrice;
    private Integer minTables;
    private Integer maxTables;
    private String hallImage;
    private String description;
    private HallStatus status;
    private List<HallPricingDTO> pricings;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
