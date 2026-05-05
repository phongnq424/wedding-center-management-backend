package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.DishComboStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class DishComboResponse {
    private UUID id;
    private String name;
    private Double comboDiscountRate;
    private Double estimatedOriginalPricePerTable;
    private Double estimatedComboPricePerTable;
    private String description;
    private DishComboStatus status;
    private List<DishComboSlotResponse> slots;
    private String slotSummary;
    private Integer numberOfSlots;
    private Integer replaceableSlotCount;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
