package com.wedding.management.domain.booking.dto;

import com.wedding.management.domain.booking.enums.BookingLineItemType;
import com.wedding.management.domain.booking.enums.BookingLineSourceType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class BookingLineResponse {
    private UUID id;
    private BookingLineItemType itemType;
    private UUID itemId;
    private String itemName;
    private Integer quantity;
    private Double unitPrice;
    private Double discountAmount;
    private Double taxRate;
    private Double taxAmount;
    private Double lineAmount;
    private BookingLineSourceType sourceType;
    private UUID sourceId;
    private String sourceName;
    private Boolean editable;
    private Boolean removable;
    private Integer displayOrder;
}
