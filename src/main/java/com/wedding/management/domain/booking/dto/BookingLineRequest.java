package com.wedding.management.domain.booking.dto;

import com.wedding.management.domain.booking.enums.BookingLineItemType;
import com.wedding.management.domain.booking.enums.BookingLineSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingLineRequest {
    @NotNull(message = "Loại dòng không được để trống")
    private BookingLineItemType itemType;

    private UUID itemId;
    private String itemName;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    private Double unitPrice;
    private Double discountAmount;
    private Double taxRate;
    private BookingLineSourceType sourceType;
    private UUID sourceId;
    private String sourceName;
    private Boolean editable;
    private Boolean removable;
    private Integer displayOrder;
}
