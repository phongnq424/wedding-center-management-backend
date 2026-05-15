package com.wedding.management.domain.payment.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.payment.dto.*;
import com.wedding.management.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payments")
@PreAuthorize("hasAuthority('PAYMENT_FULL_ACCESS')")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/booking/{bookingId}/summary")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> initializePaymentForm(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.<PaymentSummaryResponse>builder().success(true).message("Lấy thông tin thanh toán booking thành công").data(paymentService.initializePaymentForm(bookingId)).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PaymentResponse>builder().success(true).message("MSG48: Payment được tạo thành công").data(paymentService.createPayment(request, principal.getName())).build());
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePayment(@PathVariable UUID paymentId, @Valid @RequestBody PaymentUpdateRequest request, @RequestParam long lastModifiedAt, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder().success(true).message("MSG17: Payment được cập nhật thành công").data(paymentService.updatePayment(paymentId, request, lastModifiedAt, principal.getName())).build());
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(@PathVariable UUID paymentId, @Valid @RequestBody CancelPaymentRequest request, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder().success(true).message("MSG20: Payment được hủy thành công").data(paymentService.cancelPayment(paymentId, request.getReason(), principal.getName())).build());
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@PathVariable UUID paymentId, @Valid @RequestBody ProcessPaymentRequest request, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder().success(true).message("MSG6: Payment được xử lý thành công").data(paymentService.processPayment(paymentId, request, principal.getName())).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> searchPayments(@ModelAttribute PaymentSearchCriteria criteria, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<PaymentResponse> data = paymentService.searchPayments(criteria);
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder().success(true).message(data.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm payment thành công").data(toPage(data, page, Math.min(size, 20))).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder().success(true).message("Lấy danh sách payment thành công").data(paymentService.getAllPayments()).build());
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder().success(true).message("Lấy thông tin payment thành công").data(paymentService.getPaymentById(paymentId)).build());
    }

    private <T> Page<T> toPage(List<T> data, int page, int size) {
        int from = page * size;
        List<T> p = from >= data.size() ? List.of() : data.subList(from, Math.min(from + size, data.size()));
        return new PageImpl<>(p, PageRequest.of(page, size), data.size());
    }
}
