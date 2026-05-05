package com.wedding.management.domain.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelBookingRequest {
    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;
}
