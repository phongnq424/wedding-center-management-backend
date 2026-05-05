package com.wedding.management.domain.booking.service;

import java.util.UUID;

public interface BookingDocumentService {
    void generateConfirmationDocument(UUID bookingId);
    void regenerateConfirmationDocument(UUID bookingId);
    void generateCancellationDocument(UUID bookingId);
}
