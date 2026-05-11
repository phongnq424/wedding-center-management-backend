package com.wedding.management.domain.payment.dto;
import com.wedding.management.domain.payment.enums.*;
import lombok.*;
import java.time.*;
import java.util.UUID;
@Data @Builder public class PaymentResponse {
 private UUID id; private UUID bookingId; private String customerName; private String customerPhone;
 private Double bookingAmount; private Double depositAmount; private Double confirmedPaidAmount; private Double pendingPaymentAmount; private Double remainingAmount;
 private PaymentType paymentType; private Double amount; private PaymentMethod paymentMethod; private LocalDate paymentDate; private Double receivedAmount; private Double changeAmount; private String referenceNumber; private String note;
 private PaymentStatus status; private String cancelReason; private String failureReason; private Instant createdAt; private Instant processedAt; private Instant cancelledAt; private Instant lastModifiedAt; private String lastModifiedBy;
}
