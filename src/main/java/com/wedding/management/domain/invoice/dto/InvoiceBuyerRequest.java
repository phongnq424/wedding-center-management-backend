package com.wedding.management.domain.invoice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InvoiceBuyerRequest {
    @NotBlank(message = "Tên người mua không được để trống")
    private String buyerName;
    private String buyerLegalName;
    private String buyerTaxCode;
    @NotBlank(message = "Địa chỉ người mua không được để trống")
    private String buyerAddress;
    @Email(message = "Email không hợp lệ")
    private String buyerEmail;
    @NotBlank(message = "Số điện thoại người mua không được để trống")
    private String buyerPhone;
    private String buyerBankAccount;
    private String buyerBankName;
}
