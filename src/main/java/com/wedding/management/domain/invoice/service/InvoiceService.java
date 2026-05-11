package com.wedding.management.domain.invoice.service;

import com.wedding.management.domain.invoice.dto.*;

import java.util.*;

public interface InvoiceService {
    InvoiceResponse createDraft(CreateInvoiceRequest request, String currentUserId);

    InvoiceResponse updateInvoice(UUID invoiceId, UpdateInvoiceRequest request, long lastModifiedAt, String currentUserId);

    InvoiceResponse generateInvoice(UUID invoiceId, GenerateInvoiceRequest request, String currentUserId);

    InvoiceResponse cancelInvoice(UUID invoiceId, String reason, String currentUserId);

    List<InvoiceResponse> searchInvoices(InvoiceSearchCriteria criteria);

    List<InvoiceResponse> getAllInvoices();

    InvoiceResponse getInvoiceById(UUID invoiceId);
}
