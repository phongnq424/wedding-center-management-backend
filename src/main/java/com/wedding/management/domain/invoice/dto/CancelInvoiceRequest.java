package com.wedding.management.domain.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelInvoiceRequest {
    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;
}
