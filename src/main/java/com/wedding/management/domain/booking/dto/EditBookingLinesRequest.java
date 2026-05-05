package com.wedding.management.domain.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EditBookingLinesRequest {
    @Valid
    @NotNull(message = "Danh sách dòng đặt tiệc không được để trống")
    private List<BookingLineRequest> lines;
}
