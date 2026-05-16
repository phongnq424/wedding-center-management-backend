package com.wedding.management.domain.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentOtpChallengeResponse {
    private UUID paymentId;
    private String mfaChallengeId;
    private String email;
    private long expiresInSeconds;
}