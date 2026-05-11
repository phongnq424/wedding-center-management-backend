package com.wedding.management.domain.invoice.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.booking.model.Booking;
import com.wedding.management.domain.invoice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_booking", columnList = "booking_id"),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_payment_status", columnList = "payment_status"),
        @Index(name = "idx_invoice_number", columnList = "invoice_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class Invoice extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    @Column(nullable = false)
    private String buyerName;
    private String buyerLegalName;
    private String buyerTaxCode;
    @Column(columnDefinition = "TEXT")
    private String buyerAddress;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerBankAccount;
    private String buyerBankName;

    @Column(nullable = false)
    private Double subtotalAmount;
    @Column(nullable = false)
    private Double taxAmount;
    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private InvoicePaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    private String providerInvoiceId;
    private String invoiceNumber;
    private String invoiceSymbol;
    private String taxAuthorityCode;
    @Column(columnDefinition = "TEXT")
    private String pdfUrl;
    @Column(columnDefinition = "TEXT")
    private String providerErrorMessage;
    private String issuedBy;
    private Instant issuedAt;
    @Column(columnDefinition = "TEXT")
    private String cancelReason;
    private String cancelledBy;
    private Instant cancelledAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceLineSnapshot> lineSnapshots;
}
