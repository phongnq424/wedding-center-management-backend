package com.wedding.management.domain.payment.dto;
import com.wedding.management.domain.payment.enums.PaymentType;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class PaymentUpdateRequest {
 @NotNull(message="Loại thanh toán không được để trống") private PaymentType paymentType;
 @NotNull(message="Số tiền không được để trống") @Positive(message="Số tiền phải lớn hơn 0") private Double amount;
}
