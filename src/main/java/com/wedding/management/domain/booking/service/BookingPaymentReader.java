package com.wedding.management.domain.booking.service;

import java.util.UUID;

public interface BookingPaymentReader {
    double getConfirmedPaymentAmountByBooking(UUID bookingId);
    double getTotalPaidAmount(UUID bookingId);
    double getRefundableAmount(UUID bookingId, long daysBeforeWedding);
}
