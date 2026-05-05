package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.DishTypeStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class DishTypeResponse {
    private UUID id;
    private String name;
    private String description;
    private DishTypeStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
