package com.wedding.management.domain.invoice.service;

import java.util.UUID;

public interface InvoicePaymentSyncService {
    void syncPaymentStatusForBooking(UUID bookingId, String currentUserId);
}
