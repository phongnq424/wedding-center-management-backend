package com.wedding.management.domain.payment.dto;

import com.wedding.management.domain.payment.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProcessPaymentRequest {

 @NotNull(message = "Phương thức thanh toán không được để trống")
 private PaymentMethod paymentMethod;

 @NotNull(message = "Ngày thanh toán không được để trống")
 private LocalDate paymentDate;

 private String referenceNumber;

 @NotNull(message = "Số tiền nhận không được để trống")
 @PositiveOrZero(message = "Số tiền nhận không hợp lệ")
 private Double receivedAmount;

 private String note;

 @NotBlank(message = "Mã phiên xác thực không được để trống")
 private String mfaChallengeId;

 @NotBlank(message = "Mã 2FA không được để trống")
 private String inputCode;
}