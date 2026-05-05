package com.wedding.management.domain.menu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class DishComboSlotRequest {
    @NotNull(message = "Loại món ăn trong slot không được để trống")
    private UUID dishTypeId;
    @NotNull(message = "Món mặc định trong slot không được để trống")
    private UUID defaultDishId;
    private Boolean isReplaceable;
    private Integer displayOrder;
}
