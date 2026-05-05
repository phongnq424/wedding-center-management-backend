package com.wedding.management.domain.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class CancelBookingResponse {
    private UUID bookingId;
    private String customerName;
    private Double totalPaidAmount;
    private Double totalRefundAmount;
    private Double nonRefundableAmount;
    private String reason;
}
