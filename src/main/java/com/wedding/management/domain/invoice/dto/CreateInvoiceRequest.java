package com.wedding.management.domain.invoice.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateInvoiceRequest {
    @NotNull(message = "Booking không được để trống")
    private UUID bookingId;
    @Valid
    @NotNull(message = "Thông tin người mua không được để trống")
    private InvoiceBuyerRequest buyer;
}
