package com.wedding.management.domain.invoice.dto;

import com.wedding.management.domain.invoice.enums.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Data
@Builder
public class InvoiceResponse {
    private UUID id;
    private UUID bookingId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String buyerName;
    private String buyerLegalName;
    private String buyerTaxCode;
    private String buyerAddress;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerBankAccount;
    private String buyerBankName;
    private Double subtotalAmount;
    private Double taxAmount;
    private Double totalAmount;
    private InvoicePaymentStatus paymentStatus;
    private InvoiceStatus status;
    private String providerInvoiceId;
    private String invoiceNumber;
    private String invoiceSymbol;
    private String taxAuthorityCode;
    private String pdfUrl;
    private String providerErrorMessage;
    private Instant createdAt;
    private Instant issuedAt;
    private Instant cancelledAt;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
    private List<InvoiceLineResponse> lineSnapshots;
}
