package com.wedding.management.domain.booking.service.impl;

import com.wedding.management.domain.booking.service.BookingPaymentReader;
import com.wedding.management.domain.booking.strategy.RefundPolicyResolver;
import com.wedding.management.domain.booking.strategy.RefundPolicyStrategy;
import com.wedding.management.domain.payment.enums.PaymentStatus;
import com.wedding.management.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingPaymentReaderImpl implements BookingPaymentReader {

    private final PaymentRepository paymentRepository;
    private final RefundPolicyResolver refundPolicyResolver;

    public BookingPaymentReaderImpl(
            PaymentRepository paymentRepository,
            RefundPolicyResolver refundPolicyResolver
    ) {
        this.paymentRepository = paymentRepository;
        this.refundPolicyResolver = refundPolicyResolver;
    }

    @Override
    public double getConfirmedPaymentAmountByBooking(UUID bookingId) {
        return paymentRepository.sumAmountByBookingIdAndStatus(
                bookingId,
                PaymentStatus.PROCESSED
        );
    }

    @Override
    public double getPendingPaymentAmountByBooking(UUID bookingId) {
        return paymentRepository.sumAmountByBookingIdAndStatus(
                bookingId,
                PaymentStatus.UNPROCESSED
        );
    }

    @Override
    public double getTotalPaidAmount(UUID bookingId) {
        return getConfirmedPaymentAmountByBooking(bookingId);
    }

    @Override
    public double getRefundableAmount(UUID bookingId, long daysBeforeWedding) {
        double paidAmount = getConfirmedPaymentAmountByBooking(bookingId);

        RefundPolicyStrategy strategy =
                refundPolicyResolver.resolve(daysBeforeWedding);

        return strategy.calculateRefund(paidAmount);
    }
}