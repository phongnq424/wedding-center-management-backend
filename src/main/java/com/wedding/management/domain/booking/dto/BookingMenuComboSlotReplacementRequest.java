package com.wedding.management.domain.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingMenuComboSlotReplacementRequest {

    @NotNull(message = "Slot combo không được để trống")
    private UUID slotId;

    @NotNull(message = "Món thay thế không được để trống")
    private UUID dishId;
}