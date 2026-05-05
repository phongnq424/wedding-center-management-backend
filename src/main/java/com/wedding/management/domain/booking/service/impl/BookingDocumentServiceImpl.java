package com.wedding.management.domain.booking.service.impl;

import com.wedding.management.domain.booking.service.BookingDocumentService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingDocumentServiceImpl implements BookingDocumentService {
    @Override
    public void generateConfirmationDocument(UUID bookingId) {
        System.out.println("GenerateConfirmationDocument: " + bookingId);
    }

    @Override
    public void regenerateConfirmationDocument(UUID bookingId) {
        System.out.println("RegenerateConfirmationDocument: " + bookingId);
    }

    @Override
    public void generateCancellationDocument(UUID bookingId) {
        System.out.println("GenerateCancellationDocument: " + bookingId);
    }
}
