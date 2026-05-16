package com.wedding.management.domain.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BookingMenuComboSlotSnapshotResponse {
    private UUID id;
    private UUID slotId;
    private String slotName;

    private UUID originalDishId;
    private String originalDishName;
    private Double originalDishPrice;

    private UUID selectedDishId;
    private String selectedDishName;
    private Double selectedDishPrice;

    private Boolean replaced;
    private Integer displayOrder;
}