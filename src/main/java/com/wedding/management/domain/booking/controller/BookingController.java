package com.wedding.management.domain.booking.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.booking.dto.*;
import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@PreAuthorize("hasAuthority('BOOKING_FULL_ACCESS')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** UC35: Check Hall Availability */
    @GetMapping("/hall-availability")
    public ResponseEntity<ApiResponse<Page<HallAvailabilityResponse>>> checkHallAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
            @RequestParam UUID shiftId,
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int finalSize = Math.min(size, 20);
        List<HallAvailabilityResponse> halls = bookingService.checkHallAvailability(bookingDate, shiftId, capacity);
        Page<HallAvailabilityResponse> pageData = toPage(halls, page, finalSize);

        return ResponseEntity.ok(ApiResponse.<Page<HallAvailabilityResponse>>builder()
                .success(true)
                .message(halls.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm sảnh trống thành công")
                .data(pageData)
                .build());
    }

    /** UC36: Create Booking */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal
    ) {
        BookingResponse response = bookingService.createBooking(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingResponse>builder()
                        .success(true)
                        .message("MSG48: Booking được tạo thành công")
                        .data(response)
                        .build());
    }

    /** UC37: Update Booking */
    @PutMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingRequest request,
            @RequestParam long lastModifiedAt,
            Principal principal
    ) {
        BookingResponse response = bookingService.updateBooking(bookingId, request, principal.getName(), lastModifiedAt);
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("MSG17: Booking được cập nhật thành công")
                .data(response)
                .build());
    }

    /** UC38: Search Booking */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> searchBookings(
            @ModelAttribute BookingSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int finalSize = Math.min(size, 20);
        List<BookingResponse> bookings = bookingService.searchBookings(criteria);
        Page<BookingResponse> pageData = toPage(bookings, page, finalSize);

        return ResponseEntity.ok(ApiResponse.<Page<BookingResponse>>builder()
                .success(true)
                .message(bookings.isEmpty() ? "MSG12: Không tìm thấy kết quả" : "Tìm kiếm thành công")
                .data(pageData)
                .build());
    }

    /** UC39: Cancel Booking */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody CancelBookingRequest request,
            Principal principal
    ) {
        CancelBookingResponse response = bookingService.cancelBooking(bookingId, request.getReason(), principal.getName());
        return ResponseEntity.ok(ApiResponse.<CancelBookingResponse>builder()
                .success(true)
                .message("MSG20: Booking được hủy thành công")
                .data(response)
                .build());
    }

    /** UC40: Edit Booking Dish List */
    @PutMapping("/{bookingId}/dish-lines")
    public ResponseEntity<ApiResponse<BookingResponse>> updateDishLines(
            @PathVariable UUID bookingId,
            @Valid @RequestBody EditBookingLinesRequest request,
            Principal principal
    ) {
        BookingResponse response = bookingService.updateDishLines(bookingId, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("MSG17: Danh sách món ăn được cập nhật thành công")
                .data(response)
                .build());
    }

    /** UC41: Edit Booking Service List */
    @PutMapping("/{bookingId}/service-lines")
    public ResponseEntity<ApiResponse<BookingResponse>> updateServiceLines(
            @PathVariable UUID bookingId,
            @Valid @RequestBody EditBookingLinesRequest request,
            Principal principal
    ) {
        BookingResponse response = bookingService.updateServiceLines(bookingId, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("MSG17: Danh sách dịch vụ được cập nhật thành công")
                .data(response)
                .build());
    }

    /** UC42: Edit Booking Beverage List */
    @PutMapping("/{bookingId}/beverage-lines")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBeverageLines(
            @PathVariable UUID bookingId,
            @Valid @RequestBody EditBookingLinesRequest request,
            Principal principal
    ) {
        BookingResponse response = bookingService.updateBeverageLines(bookingId, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("MSG17: Danh sách đồ uống được cập nhật thành công")
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder()
                .success(true)
                .message("Lấy danh sách booking thành công")
                .data(bookings)
                .build());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable UUID bookingId) {
        BookingResponse booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Lấy thông tin booking thành công")
                .data(booking)
                .build());
    }

    private <T> Page<T> toPage(List<T> data, int page, int size) {
        int fromIndex = page * size;
        List<T> paginated;
        if (fromIndex >= data.size()) {
            paginated = List.of();
        } else {
            int toIndex = Math.min(fromIndex + size, data.size());
            paginated = data.subList(fromIndex, toIndex);
        }
        return new PageImpl<>(paginated, PageRequest.of(page, size), data.size());
    }
}
