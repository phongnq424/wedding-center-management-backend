package com.wedding.management.domain.payment.dto;
import com.wedding.management.domain.payment.enums.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
@Data public class PaymentSearchCriteria {
 private String paymentId; private String bookingId; private String customerName; private String customerPhone;
 private PaymentType paymentType; private PaymentMethod paymentMethod; private PaymentStatus status;
 @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate paymentDateFrom;
 @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate paymentDateTo;
 private Double amountFrom; private Double amountTo; private String referenceNumber;
}
