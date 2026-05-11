package com.wedding.management.domain.invoice.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.invoice.dto.*;
import com.wedding.management.domain.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/invoices")
@PreAuthorize("hasRole('OPERATIONS_MANAGER')")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> createDraft(@Valid @RequestBody CreateInvoiceRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<InvoiceResponse>builder().success(true).message("MSG48: Invoice draft được tạo thành công").data(invoiceService.createDraft(request, principal.getName())).build());
    }

    @PutMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody UpdateInvoiceRequest request, @RequestParam long lastModifiedAt, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("MSG17: Invoice được cập nhật thành công").data(invoiceService.updateInvoice(invoiceId, request, lastModifiedAt, principal.getName())).build());
    }

    @PostMapping("/{invoiceId}/generate")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody GenerateInvoiceRequest request, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("MSG17: Invoice được phát hành thành công").data(invoiceService.generateInvoice(invoiceId, request, principal.getName())).build());
    }

    @PostMapping("/{invoiceId}/cancel")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancelInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody CancelInvoiceRequest request, Principal principal) {
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("MSG20: Invoice được hủy thành công").data(invoiceService.cancelInvoice(invoiceId, request.getReason(), principal.getName())).build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> searchInvoices(@ModelAttribute InvoiceSearchCriteria criteria, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<InvoiceResponse> data = invoiceService.searchInvoices(criteria);
        return ResponseEntity.ok(ApiResponse.<Page<InvoiceResponse>>builder().success(true).message(data.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm invoice thành công").data(toPage(data, page, Math.min(size, 20))).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResponse.<List<InvoiceResponse>>builder().success(true).message("Lấy danh sách invoice thành công").data(invoiceService.getAllInvoices()).build());
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Lấy thông tin invoice thành công").data(invoiceService.getInvoiceById(invoiceId)).build());
    }

    private <T> Page<T> toPage(List<T> data, int page, int size) {
        int from = page * size;
        List<T> p = from >= data.size() ? List.of() : data.subList(from, Math.min(from + size, data.size()));
        return new PageImpl<>(p, PageRequest.of(page, size), data.size());
    }
}
