package com.wedding.management.domain.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingMenuComboRequest {

    @NotNull(message = "Combo món ăn không được để trống")
    private UUID comboId;

    @NotNull(message = "Số bàn áp dụng combo không được để trống")
    @Min(value = 1, message = "Số bàn áp dụng combo phải lớn hơn 0")
    private Integer tableCount;

    /**
     * Danh sách món thay thế theo slot.
     * Nếu slot không có trong danh sách này thì dùng món gốc của combo.
     */
    @Valid
    private List<BookingMenuComboSlotReplacementRequest> slotReplacements;
}