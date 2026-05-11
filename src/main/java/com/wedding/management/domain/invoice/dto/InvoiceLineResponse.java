package com.wedding.management.domain.invoice.dto;

import com.wedding.management.domain.booking.enums.BookingLineItemType;
import lombok.*;

import java.util.UUID;

@Data
@Builder
public class InvoiceLineResponse {
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
    private Integer displayOrder;
}
