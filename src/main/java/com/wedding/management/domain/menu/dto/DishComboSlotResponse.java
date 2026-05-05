package com.wedding.management.domain.menu.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class DishComboSlotResponse {
    private UUID id;
    private UUID dishTypeId;
    private String dishTypeName;
    private UUID defaultDishId;
    private String defaultDishName;
    private Boolean isReplaceable;
    private Integer displayOrder;
}
