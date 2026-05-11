package com.wedding.management.domain.payment.service.impl;

import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.model.Booking;
import com.wedding.management.domain.booking.repository.BookingRepository;
import com.wedding.management.domain.booking.service.BookingPaymentReader;
import com.wedding.management.domain.invoice.service.InvoicePaymentSyncService;
import com.wedding.management.domain.payment.dto.*;
import com.wedding.management.domain.payment.enums.*;
import com.wedding.management.domain.payment.model.Payment;
import com.wedding.management.domain.payment.repository.PaymentRepository;
import com.wedding.management.domain.payment.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {
    private static final double EPS = 0.0001;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final InvoicePaymentSyncService invoicePaymentSyncService;
    private final TwoFactorVerificationService twoFactorVerificationService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository, InvoicePaymentSyncService invoicePaymentSyncService, TwoFactorVerificationService twoFactorVerificationService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.invoicePaymentSyncService = invoicePaymentSyncService;
        this.twoFactorVerificationService = twoFactorVerificationService;
    }

    @Override
    public PaymentSummaryResponse initializePaymentForm(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        return summary(booking);
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request, String currentUserId) {
        Booking booking = getBooking(request.getBookingId());
        validateBookingUsable(booking);
        validatePaymentAmount(booking, request.getPaymentType(), request.getAmount());
        checkDuplicateUnprocessed(booking.getId(), request.getPaymentType(), null);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentType(request.getPaymentType())
                .amount(request.getAmount())
                .status(PaymentStatus.UNPROCESSED)
                .build();

        Payment saved = paymentRepository.save(payment);
        saveAuditLog(currentUserId, "CREATE_PAYMENT", saved.getId(), booking.getCustomerName());
        return toResponse(saved);
    }

    @Override
    public PaymentResponse updatePayment(UUID paymentId, PaymentUpdateRequest request, long lastModifiedAt, String currentUserId) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.UNPROCESSED)
            throw bad("MSG28: Payment không thể cập nhật vì trạng thái đã bị khóa");
        checkVersion(payment, lastModifiedAt);
        Booking booking = payment.getBooking();
        validateBookingUsable(booking);
        validatePaymentAmount(booking, request.getPaymentType(), request.getAmount());
        checkDuplicateUnprocessed(booking.getId(), request.getPaymentType(), paymentId);

        payment.setPaymentType(request.getPaymentType());
        payment.setAmount(request.getAmount());
        Payment saved = paymentRepository.save(payment);
        saveAuditLog(currentUserId, "UPDATE_PAYMENT", saved.getId(), booking.getCustomerName());
        return toResponse(saved);
    }

    @Override
    public PaymentResponse cancelPayment(UUID paymentId, String reason, String currentUserId) {
        if (!StringUtils.hasText(reason)) throw bad("MSG2: Lý do hủy không được để trống");
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.UNPROCESSED) throw bad("MSG67: Chỉ payment UNPROCESSED mới được hủy");
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancelReason(reason.trim());
        payment.setCancelledBy(currentUserId);
        payment.setCancelledAt(Instant.now());
        Payment saved = paymentRepository.save(payment);
        recalculateBookingPaymentSummary(saved.getBooking());
        saveAuditLog(currentUserId, "CANCEL_PAYMENT", saved.getId(), saved.getBooking().getCustomerName());
        return toResponse(saved);
    }

    @Override
    public PaymentResponse processPayment(UUID paymentId, ProcessPaymentRequest request, String currentUserId) {
        Payment payment = getPayment(paymentId);
        Booking booking = payment.getBooking();
        if (payment.getStatus() != PaymentStatus.UNPROCESSED)
            throw bad("MSG5: Payment đã được xử lý hoặc không còn hợp lệ");
        if (!(booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED))
            throw bad("MSG5: Booking không ở trạng thái cho phép xử lý payment");
        if (request.getPaymentDate().isAfter(LocalDate.now()))
            throw bad("MSG37: Ngày thanh toán không được ở tương lai");
        if (!twoFactorVerificationService.verify(currentUserId, request.getInputCode()))
            throw bad("MSG56: Mã 2FA không hợp lệ hoặc đã hết hạn");

        double receivedAmount;
        double changeAmount;
        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            if (request.getReceivedAmount() + EPS < payment.getAmount())
                throw bad("MSG69: Số tiền nhận phải lớn hơn hoặc bằng số tiền thanh toán");
            receivedAmount = request.getReceivedAmount();
            changeAmount = receivedAmount - payment.getAmount();
        } else {
            if (!StringUtils.hasText(request.getReferenceNumber()))
                throw bad("MSG2: Mã tham chiếu không được để trống với chuyển khoản/thẻ");
            receivedAmount = payment.getAmount();
            changeAmount = 0.0;
        }

        double confirmedBefore = safe(paymentRepository.sumAmountByBookingAndStatus(booking.getId(), PaymentStatus.PROCESSED));
        if (confirmedBefore + payment.getAmount() - booking.getBookingAmount() > EPS)
            throw bad("MSG5: Số tiền xử lý vượt quá tổng booking");
        if (payment.getPaymentType() == PaymentType.DEPOSIT && paymentRepository.countByBookingAndTypeAndStatus(booking.getId(), PaymentType.DEPOSIT, PaymentStatus.PROCESSED) > 0)
            throw bad("MSG68: Booking đã có deposit đã xử lý");

        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setReferenceNumber(trim(request.getReferenceNumber()));
        payment.setReceivedAmount(receivedAmount);
        payment.setChangeAmount(changeAmount);
        payment.setNote(trim(request.getNote()));
        payment.setStatus(PaymentStatus.PROCESSED);
        payment.setProcessedBy(currentUserId);
        payment.setProcessedAt(Instant.now());
        Payment saved = paymentRepository.save(payment);

        recalculateBookingPaymentSummary(booking);
        if (booking.getStatus() == BookingStatus.PENDING && safe(booking.getConfirmedPaymentAmount()) + EPS >= safe(booking.getDepositAmount()))
            booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        invoicePaymentSyncService.syncPaymentStatusForBooking(booking.getId(), currentUserId);
        saveAuditLog(currentUserId, "PROCESS_PAYMENT", saved.getId(), booking.getCustomerName());
        return toResponse(saved);
    }

    @Override
    public List<PaymentResponse> searchPayments(PaymentSearchCriteria c) {
        return paymentRepository.findAllActive().stream().filter(p -> match(c, p)).map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllActive().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(UUID paymentId) {
        return toResponse(getPayment(paymentId));
    }

    private void recalculateBookingPaymentSummary(Booking booking) {
        double confirmed = safe(paymentRepository.sumAmountByBookingAndStatus(booking.getId(), PaymentStatus.PROCESSED));
        booking.setConfirmedPaymentAmount(confirmed);
        booking.setRemainingAmount(Math.max(0, safe(booking.getBookingAmount()) - confirmed));
        bookingRepository.save(booking);
    }

    private void validatePaymentAmount(Booking b, PaymentType t, Double amount) {
        if (amount == null || amount <= 0) throw bad("MSG2: Số tiền không hợp lệ");
        double remaining = safe(b.getRemainingAmount());
        if (t == PaymentType.DEPOSIT && amount + EPS < safe(b.getDepositAmount()))
            throw bad("MSG38: Deposit phải lớn hơn hoặc bằng số tiền cọc của booking");
        if (t == PaymentType.FINAL_PAYMENT && Math.abs(amount - remaining) > EPS)
            throw bad("MSG39: Final payment phải bằng đúng số tiền còn lại");
        if (t == PaymentType.PARTIAL_PAYMENT && amount + EPS >= remaining)
            throw bad("MSG19: Partial payment phải nhỏ hơn số tiền còn lại, hãy dùng FINAL_PAYMENT");
    }

    private void checkDuplicateUnprocessed(UUID bookingId, PaymentType type, UUID excludePaymentId) {
        if (paymentRepository.countByBookingAndTypeAndStatus(bookingId, type, PaymentStatus.UNPROCESSED, excludePaymentId) > 0)
            throw bad("MSG68: Đã tồn tại payment UNPROCESSED cùng loại cho booking này");
    }

    private Booking getBooking(UUID id) {
        return bookingRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> notFound("Booking không tồn tại"));
    }

    private Payment getPayment(UUID id) {
        return paymentRepository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> notFound("Payment không tồn tại"));
    }

    private void validateBookingUsable(Booking b) {
        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.DELETED)
            throw bad("Booking không hợp lệ để thanh toán");
    }

    private void checkVersion(Payment p, long client) {
        if (p.getUpdatedAt() != null && p.getUpdatedAt().toEpochMilli() != client)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "MSG62: Dữ liệu đã được người khác cập nhật");
    }

    private PaymentSummaryResponse summary(Booking b) {
        return PaymentSummaryResponse.builder().bookingId(b.getId()).customerName(b.getCustomerName()).customerPhone(b.getCustomerPhone()).bookingAmount(safe(b.getBookingAmount())).depositAmount(safe(b.getDepositAmount())).confirmedPaidAmount(safe(b.getConfirmedPaymentAmount())).pendingPaymentAmount(safe(paymentRepository.sumAmountByBookingAndStatus(b.getId(), PaymentStatus.UNPROCESSED))).remainingAmount(safe(b.getRemainingAmount())).build();
    }

    private PaymentResponse toResponse(Payment p) {
        Booking b = p.getBooking();
        return PaymentResponse.builder().id(p.getId()).bookingId(b.getId()).customerName(b.getCustomerName()).customerPhone(b.getCustomerPhone()).bookingAmount(safe(b.getBookingAmount())).depositAmount(safe(b.getDepositAmount())).confirmedPaidAmount(safe(b.getConfirmedPaymentAmount())).pendingPaymentAmount(safe(paymentRepository.sumAmountByBookingAndStatus(b.getId(), PaymentStatus.UNPROCESSED))).remainingAmount(safe(b.getRemainingAmount())).paymentType(p.getPaymentType()).amount(p.getAmount()).paymentMethod(p.getPaymentMethod()).paymentDate(p.getPaymentDate()).receivedAmount(p.getReceivedAmount()).changeAmount(p.getChangeAmount()).referenceNumber(p.getReferenceNumber()).note(p.getNote()).status(p.getStatus()).cancelReason(p.getCancelReason()).failureReason(p.getFailureReason()).createdAt(p.getCreatedAt()).processedAt(p.getProcessedAt()).cancelledAt(p.getCancelledAt()).lastModifiedAt(p.getUpdatedAt()).lastModifiedBy(String.valueOf(p.getUpdatedBy())).build();
    }

    private boolean match(PaymentSearchCriteria c, Payment p) {
        Booking b = p.getBooking();
        return contains(p.getId(), c.getPaymentId()) && contains(b.getId(), c.getBookingId()) && contains(b.getCustomerName(), c.getCustomerName()) && contains(b.getCustomerPhone(), c.getCustomerPhone()) && (c.getPaymentType() == null || p.getPaymentType() == c.getPaymentType()) && (c.getPaymentMethod() == null || p.getPaymentMethod() == c.getPaymentMethod()) && (c.getStatus() == null || p.getStatus() == c.getStatus()) && (c.getAmountFrom() == null || p.getAmount() >= c.getAmountFrom()) && (c.getAmountTo() == null || p.getAmount() <= c.getAmountTo()) && contains(p.getReferenceNumber(), c.getReferenceNumber()) && (c.getPaymentDateFrom() == null || (p.getPaymentDate() != null && !p.getPaymentDate().isBefore(c.getPaymentDateFrom()))) && (c.getPaymentDateTo() == null || (p.getPaymentDate() != null && !p.getPaymentDate().isAfter(c.getPaymentDateTo())));
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
