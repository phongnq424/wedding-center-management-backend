package com.wedding.management.domain.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingMenuComboSnapshotResponse {
    private UUID id;
    private UUID comboId;
    private String comboName;
    private Integer tableCount;
    private Double originalComboPrice;
    private Double discountRate;
    private Double discountedComboPrice;
    private Integer displayOrder;
    private List<BookingMenuComboSlotSnapshotResponse> slotSnapshots;
}