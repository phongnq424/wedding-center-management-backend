package com.wedding.management.domain.payment.service;

import com.wedding.management.domain.payment.dto.*;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentSummaryResponse initializePaymentForm(UUID bookingId);

    PaymentResponse createPayment(PaymentRequest request, String currentUserId);

    PaymentResponse updatePayment(
            UUID paymentId,
            PaymentUpdateRequest request,
            long lastModifiedAt,
            String currentUserId
    );

    PaymentResponse cancelPayment(
            UUID paymentId,
            String reason,
            String currentUserId
    );

    PaymentOtpChallengeResponse createProcessPaymentOtp(
            UUID paymentId,
            String currentUserId
    );

    PaymentResponse processPayment(
            UUID paymentId,
            ProcessPaymentRequest request,
            String currentUserId
    );

    List<PaymentResponse> searchPayments(PaymentSearchCriteria criteria);

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(UUID paymentId);
}