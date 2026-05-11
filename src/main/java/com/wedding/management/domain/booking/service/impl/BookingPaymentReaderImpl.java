package com.wedding.management.domain.booking.service.impl;

import com.wedding.management.domain.booking.service.BookingPaymentReader;
import com.wedding.management.domain.payment.enums.PaymentStatus;
import com.wedding.management.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingPaymentReaderImpl implements BookingPaymentReader {

    private final PaymentRepository paymentRepository;

    public BookingPaymentReaderImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
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

        if (daysBeforeWedding >= 30) {
            return paidAmount;
        }

        if (daysBeforeWedding >= 14) {
            return paidAmount * 0.5;
        }

        return 0.0;
    }
}