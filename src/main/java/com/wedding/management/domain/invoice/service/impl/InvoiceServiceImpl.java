package com.wedding.management.domain.invoice.service.impl;

import com.wedding.management.domain.booking.enums.BookingLineItemType;
import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.model.Booking;
import com.wedding.management.domain.booking.model.BookingLineSnapshot;
import com.wedding.management.domain.booking.repository.BookingLineSnapshotRepository;
import com.wedding.management.domain.booking.repository.BookingRepository;
import com.wedding.management.domain.invoice.dto.*;
import com.wedding.management.domain.invoice.enums.*;
import com.wedding.management.domain.invoice.model.*;
import com.wedding.management.domain.invoice.repository.*;
import com.wedding.management.domain.invoice.service.*;
import com.wedding.management.domain.payment.repository.PaymentRepository;
import com.wedding.management.domain.payment.enums.PaymentStatus;
import com.wedding.management.domain.payment.service.TwoFactorVerificationService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService, InvoicePaymentSyncService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineSnapshotRepository invoiceLineRepository;
    private final BookingRepository bookingRepository;
    private final BookingLineSnapshotRepository bookingLineRepository;
    private final PaymentRepository paymentRepository;
    private final TwoFactorVerificationService twoFactorVerificationService;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, InvoiceLineSnapshotRepository invoiceLineRepository, BookingRepository bookingRepository, BookingLineSnapshotRepository bookingLineRepository, PaymentRepository paymentRepository, TwoFactorVerificationService twoFactorVerificationService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.bookingRepository = bookingRepository;
        this.bookingLineRepository = bookingLineRepository;
        this.paymentRepository = paymentRepository;
        this.twoFactorVerificationService = twoFactorVerificationService;
    }

    @Override
    public InvoiceResponse createDraft(CreateInvoiceRequest request, String currentUserId) {
        Booking booking = getBooking(request.getBookingId());
        if (booking.getStatus() != BookingStatus.COMPLETED)
            throw bad("MSG70: Chỉ booking COMPLETED mới được tạo invoice");
        List<InvoiceStatus> active = List.of(InvoiceStatus.DRAFT, InvoiceStatus.ISSUED, InvoiceStatus.REJECTED);
        if (!invoiceRepository.findActiveByBookingAndStatuses(booking.getId(), active).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "MSG32: Booking đã có invoice đang hoạt động");
        validateBuyer(request.getBuyer());

        Invoice invoice = Invoice.builder()
                .booking(booking)
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .customerEmail(booking.getCustomerEmail())
                .buyerName(trim(request.getBuyer().getBuyerName()))
                .buyerLegalName(trim(request.getBuyer().getBuyerLegalName()))
                .buyerTaxCode(trim(request.getBuyer().getBuyerTaxCode()))
                .buyerAddress(trim(request.getBuyer().getBuyerAddress()))
                .buyerEmail(trim(request.getBuyer().getBuyerEmail()))
                .buyerPhone(trim(request.getBuyer().getBuyerPhone()))
                .buyerBankAccount(trim(request.getBuyer().getBuyerBankAccount()))
                .buyerBankName(trim(request.getBuyer().getBuyerBankName()))
                .subtotalAmount(safe(booking.getSubtotalAmount()))
                .taxAmount(safe(booking.getTaxAmount()))
                .totalAmount(safe(booking.getBookingAmount()))
                .paymentStatus(calculatePaymentStatus(booking))
                .status(InvoiceStatus.DRAFT)
                .build();
        Invoice saved = invoiceRepository.save(invoice);
        copyBookingLinesToInvoice(saved, booking.getId());
        saveAuditLog(currentUserId, "CREATE_INVOICE", saved.getId(), booking.getCustomerName());
        return toResponse(saved);
    }

    @Override
    public InvoiceResponse updateInvoice(UUID invoiceId, UpdateInvoiceRequest request, long lastModifiedAt, String currentUserId) {
        Invoice invoice = getInvoice(invoiceId);
        if (!(invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.REJECTED))
            throw bad("MSG28: Invoice đã phát hành/hủy không thể cập nhật");
        checkVersion(invoice, lastModifiedAt);
        validateBuyer(request.getBuyer());
        applyBuyer(invoice, request.getBuyer());
        invoice.setPaymentStatus(calculatePaymentStatus(invoice.getBooking()));
        Invoice saved = invoiceRepository.save(invoice);
        saveAuditLog(currentUserId, "UPDATE_INVOICE", saved.getId(), saved.getCustomerName());
        return toResponse(saved);
    }

    @Override
    public InvoiceResponse generateInvoice(UUID invoiceId, GenerateInvoiceRequest request, String currentUserId) {
        Invoice invoice = getInvoice(invoiceId);
        if (!(invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.REJECTED))
            throw bad("MSG72: Chỉ invoice DRAFT hoặc REJECTED mới được phát hành");
        if (!twoFactorVerificationService.verify(
                currentUserId,
                request.getMfaChallengeId(),
                request.getInputCode()
        )) {
            throw bad("MSG56: Mã 2FA không hợp lệ hoặc đã hết hạn");
        }
        if (invoiceLineRepository.findByInvoiceId(invoice.getId()).isEmpty())
            throw bad("MSG72: Invoice không có dòng snapshot");

        Instant now = Instant.now();
        String suffix = invoice.getId().toString().substring(0, 8).toUpperCase();
        invoice.setProviderInvoiceId("PROVIDER-" + suffix);
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setInvoiceSymbol("VAT-" + Year.now().getValue());
        invoice.setTaxAuthorityCode("TAX-" + suffix);
        invoice.setPdfUrl("/api/v1/invoices/" + invoice.getId() + "/pdf");
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedBy(currentUserId);
        invoice.setIssuedAt(now);
        invoice.setProviderErrorMessage(null);
        invoice.setPaymentStatus(calculatePaymentStatus(invoice.getBooking()));
        Invoice saved = invoiceRepository.save(invoice);
        saveAuditLog(currentUserId, "GENERATE_INVOICE", saved.getId(), saved.getCustomerName());
        return toResponse(saved);
    }

    @Override
    public InvoiceResponse cancelInvoice(UUID invoiceId, String reason, String currentUserId) {
        if (!StringUtils.hasText(reason)) throw bad("MSG2: Lý do hủy không được để trống");
        Invoice invoice = getInvoice(invoiceId);
        if (!(invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.REJECTED))
            throw bad("MSG67: Chỉ invoice DRAFT hoặc REJECTED mới được hủy");
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelReason(reason.trim());
        invoice.setCancelledBy(currentUserId);
        invoice.setCancelledAt(Instant.now());
        Invoice saved = invoiceRepository.save(invoice);
        saveAuditLog(currentUserId, "CANCEL_INVOICE", saved.getId(), saved.getCustomerName());
        return toResponse(saved);
    }

    @Override
    public List<InvoiceResponse> searchInvoices(InvoiceSearchCriteria c) {
        return invoiceRepository.findAllActive().stream().filter(i -> match(c, i)).map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAllActive().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public InvoiceResponse getInvoiceById(UUID invoiceId) {
        return toResponse(getInvoice(invoiceId));
    }

    @Override
    public void syncPaymentStatusForBooking(UUID bookingId, String currentUserId) {
        List<Invoice> invoices = invoiceRepository.findByBookingId(bookingId);
        for (Invoice invoice : invoices) {
            if (invoice.getStatus() == InvoiceStatus.CANCELLED) continue;
            invoice.setPaymentStatus(calculatePaymentStatus(invoice.getBooking()));
            invoiceRepository.save(invoice);
        }
    }

    private void copyBookingLinesToInvoice(Invoice invoice, UUID bookingId) {
        List<BookingLineSnapshot> source = bookingLineRepository.findByBookingId(bookingId);
        List<InvoiceLineSnapshot> lines = source.stream().map(line -> InvoiceLineSnapshot.builder()
                .invoice(invoice)
                .itemType(line.getItemType())
                .itemId(line.getItemId())
                .itemName(line.getItemName())
                .quantity(line.getQuantity())
                .unitPrice(safe(line.getUnitPrice()))
                .discountAmount(safe(line.getDiscountAmount()))
                .taxRate(safe(line.getTaxRate()))
                .taxAmount(safe(line.getTaxAmount()))
                .lineAmount(safe(line.getLineAmount()))
                .displayOrder(line.getDisplayOrder())
                .build()).collect(Collectors.toList());
        invoiceLineRepository.saveAll(lines);
    }

    private InvoicePaymentStatus calculatePaymentStatus(Booking booking) {
        double confirmed = safe(paymentRepository.sumAmountByBookingAndStatus(booking.getId(), PaymentStatus.PROCESSED));
        double total = safe(booking.getBookingAmount());
        double remaining = Math.max(0, total - confirmed);
        if (confirmed <= 0) return InvoicePaymentStatus.UNPAID;
        if (remaining > 0.0001) return InvoicePaymentStatus.PARTIALLY_PAID;
        return InvoicePaymentStatus.PAID;
    }

    private InvoiceResponse toResponse(Invoice i) {
        List<InvoiceLineResponse> lines = invoiceLineRepository.findByInvoiceId(i.getId()).stream().map(this::toLineResponse).collect(Collectors.toList());
        return InvoiceResponse.builder()
                .id(i.getId()).bookingId(i.getBooking().getId()).customerName(i.getCustomerName()).customerPhone(i.getCustomerPhone()).customerEmail(i.getCustomerEmail())
                .buyerName(i.getBuyerName()).buyerLegalName(i.getBuyerLegalName()).buyerTaxCode(i.getBuyerTaxCode()).buyerAddress(i.getBuyerAddress()).buyerEmail(i.getBuyerEmail()).buyerPhone(i.getBuyerPhone()).buyerBankAccount(i.getBuyerBankAccount()).buyerBankName(i.getBuyerBankName())
                .subtotalAmount(i.getSubtotalAmount()).taxAmount(i.getTaxAmount()).totalAmount(i.getTotalAmount()).paymentStatus(i.getPaymentStatus()).status(i.getStatus())
                .providerInvoiceId(i.getProviderInvoiceId()).invoiceNumber(i.getInvoiceNumber()).invoiceSymbol(i.getInvoiceSymbol()).taxAuthorityCode(i.getTaxAuthorityCode()).pdfUrl(i.getPdfUrl()).providerErrorMessage(i.getProviderErrorMessage())
                .createdAt(i.getCreatedAt()).issuedAt(i.getIssuedAt()).cancelledAt(i.getCancelledAt()).lastModifiedAt(i.getUpdatedAt()).lastModifiedBy(String.valueOf(i.getUpdatedBy())).lineSnapshots(lines)
                .build();
    }

    private InvoiceLineResponse toLineResponse(InvoiceLineSnapshot l) {
        return InvoiceLineResponse.builder().id(l.getId()).itemType(l.getItemType()).itemId(l.getItemId()).itemName(l.getItemName()).quantity(l.getQuantity()).unitPrice(l.getUnitPrice()).discountAmount(l.getDiscountAmount()).taxRate(l.getTaxRate()).taxAmount(l.getTaxAmount()).lineAmount(l.getLineAmount()).displayOrder(l.getDisplayOrder()).build();
    }

    private Booking getBooking(UUID id) {
        return bookingRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> notFound("Booking không tồn tại"));
    }

    private Invoice getInvoice(UUID id) {
        return invoiceRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> notFound("Invoice không tồn tại"));
    }

    private void validateBuyer(InvoiceBuyerRequest b) {
        if (
                !StringUtils.hasText(b.getBuyerName())
                        || !StringUtils.hasText(b.getBuyerAddress())
                        || !StringUtils.hasText(b.getBuyerPhone())
        ) {
            throw bad("MSG2: Thiếu thông tin người mua bắt buộc");
        }

        if (
                StringUtils.hasText(b.getBuyerTaxCode())
                        && !b.getBuyerTaxCode().matches("[0-9A-Za-z-]{5,20}")
        ) {
            throw bad("MSG71: Mã số thuế không hợp lệ");
        }
    }

    private void applyBuyer(Invoice i, InvoiceBuyerRequest b) {
        i.setBuyerName(trim(b.getBuyerName()));
        i.setBuyerLegalName(trim(b.getBuyerLegalName()));
        i.setBuyerTaxCode(trim(b.getBuyerTaxCode()));
        i.setBuyerAddress(trim(b.getBuyerAddress()));
        i.setBuyerEmail(trim(b.getBuyerEmail()));
        i.setBuyerPhone(trim(b.getBuyerPhone()));
        i.setBuyerBankAccount(trim(b.getBuyerBankAccount()));
        i.setBuyerBankName(trim(b.getBuyerBankName()));
    }

    private void checkVersion(Invoice i, long client) {
        if (i.getUpdatedAt() != null && i.getUpdatedAt().toEpochMilli() != client)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "MSG62: Dữ liệu đã được người khác cập nhật");
    }

    private boolean match(InvoiceSearchCriteria c, Invoice i) {
        return contains(i.getId(), c.getInvoiceId()) && contains(i.getBooking().getId(), c.getBookingId()) && contains(i.getCustomerName(), c.getCustomerName()) && contains(i.getCustomerPhone(), c.getCustomerPhone()) && contains(i.getBuyerName(), c.getBuyerName()) && contains(i.getBuyerLegalName(), c.getBuyerLegalName()) && contains(i.getBuyerTaxCode(), c.getBuyerTaxCode()) && contains(i.getInvoiceNumber(), c.getInvoiceNumber()) && contains(i.getInvoiceSymbol(), c.getInvoiceSymbol()) && contains(i.getTaxAuthorityCode(), c.getTaxAuthorityCode()) && (c.getPaymentStatus() == null || i.getPaymentStatus() == c.getPaymentStatus()) && (c.getStatus() == null || i.getStatus() == c.getStatus()) && (c.getTotalAmountFrom() == null || i.getTotalAmount() >= c.getTotalAmountFrom()) && (c.getTotalAmountTo() == null || i.getTotalAmount() <= c.getTotalAmountTo()) && (c.getIssuedDateFrom() == null || (i.getIssuedAt() != null && !LocalDateTime.ofInstant(i.getIssuedAt(), ZoneId.systemDefault()).toLocalDate().isBefore(c.getIssuedDateFrom()))) && (c.getIssuedDateTo() == null || (i.getIssuedAt() != null && !LocalDateTime.ofInstant(i.getIssuedAt(), ZoneId.systemDefault()).toLocalDate().isAfter(c.getIssuedDateTo())));
    }

    private boolean contains(UUID id, String q) {
        return !StringUtils.hasText(q) || (id != null && id.toString().toLowerCase().contains(q.toLowerCase()));
    }

    private boolean contains(String s, String q) {
        return !StringUtils.hasText(q) || (s != null && s.toLowerCase().contains(q.toLowerCase()));
    }

    private double safe(Double v) {
        return v == null ? 0.0 : v;
    }

    private String trim(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }

    private ResponseStatusException notFound(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }

    private void saveAuditLog(String u, String a, UUID id, String name) {
    }
}
