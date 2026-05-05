package com.wedding.management.domain.hall.dto;

import com.wedding.management.domain.hall.enums.HallTypeStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class HallTypeResponse {
    private UUID id;
    private String name;
    private String description;
    private Double basePrice;
    private HallTypeStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
