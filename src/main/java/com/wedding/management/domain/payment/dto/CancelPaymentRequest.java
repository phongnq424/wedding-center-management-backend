package com.wedding.management.domain.payment.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class CancelPaymentRequest { @NotBlank(message="Lý do hủy không được để trống") private String reason; }
