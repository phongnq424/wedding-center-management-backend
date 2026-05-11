package com.wedding.management.domain.invoice.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateInvoiceRequest {
    @Valid
    @NotNull(message = "Thông tin người mua không được để trống")
    private InvoiceBuyerRequest buyer;
}
