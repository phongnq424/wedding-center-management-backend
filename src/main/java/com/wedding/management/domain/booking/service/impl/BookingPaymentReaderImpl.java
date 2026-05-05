package com.wedding.management.domain.booking.service.impl;

import com.wedding.management.domain.booking.service.BookingPaymentReader;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingPaymentReaderImpl implements BookingPaymentReader {
    /**
     * Placeholder until Payment module exists.
     * Replace with PaymentRepository queries when UC Payment is implemented.
     */
    @Override
    public double getConfirmedPaymentAmountByBooking(UUID bookingId) {
        return 0.0;
    }

    @Override
    public double getTotalPaidAmount(UUID bookingId) {
        return 0.0;
    }

    @Override
    public double getRefundableAmount(UUID bookingId, long daysBeforeWedding) {
        return 0.0;
    }
}
