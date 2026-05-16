package com.wedding.management.domain.payment.service;

import com.wedding.management.domain.payment.dto.PaymentOtpChallengeResponse;

import java.util.UUID;

public interface TwoFactorVerificationService {

    PaymentOtpChallengeResponse createPaymentProcessingChallenge(UUID paymentId, String currentUserId);

    boolean verify(String currentUserId, String challengeId, String inputCode);
}