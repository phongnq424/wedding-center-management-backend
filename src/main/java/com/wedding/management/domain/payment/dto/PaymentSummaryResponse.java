package com.wedding.management.domain.payment.dto;
import lombok.*;
import java.util.UUID;
@Data @Builder public class PaymentSummaryResponse { private UUID bookingId; private String customerName; private String customerPhone; private Double bookingAmount; private Double depositAmount; private Double confirmedPaidAmount; private Double pendingPaymentAmount; private Double remainingAmount; }
