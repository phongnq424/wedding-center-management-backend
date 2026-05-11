package com.wedding.management.domain.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateInvoiceRequest {
    @NotBlank(message = "Mã 2FA không được để trống")
    private String inputCode;
}
