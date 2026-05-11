package com.wedding.management.domain.invoice.dto;

import com.wedding.management.domain.invoice.enums.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class InvoiceSearchCriteria {
    private String invoiceId;
    private String bookingId;
    private String customerName;
    private String customerPhone;
    private String buyerName;
    private String buyerLegalName;
    private String buyerTaxCode;
    private String invoiceNumber;
    private String invoiceSymbol;
    private String taxAuthorityCode;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedDateTo;
    private Double totalAmountFrom;
    private Double totalAmountTo;
    private InvoicePaymentStatus paymentStatus;
    private InvoiceStatus status;
}
