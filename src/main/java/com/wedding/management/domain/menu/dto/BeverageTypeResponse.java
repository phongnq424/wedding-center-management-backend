package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class BeverageTypeResponse {
    private UUID id;
    private String name;
    private String description;
    private BeverageTypeStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
