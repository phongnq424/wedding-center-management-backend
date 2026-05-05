package com.wedding.management.domain.booking.service;

import com.wedding.management.domain.booking.dto.*;
import com.wedding.management.domain.booking.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    // UC35: Check Hall Availability
    List<HallAvailabilityResponse> checkHallAvailability(LocalDate bookingDate, UUID shiftId, Integer capacity);

    // UC36: Create Booking
    BookingResponse createBooking(BookingRequest request, String currentUserId);

    // UC37: Update Booking
    BookingResponse updateBooking(UUID bookingId, BookingRequest request, String currentUserId, long lastModifiedAt);

    // UC38: Search Booking
    List<BookingResponse> searchBookings(BookingSearchCriteria criteria);

    // UC39: Cancel Booking
    CancelBookingResponse cancelBooking(UUID bookingId, String reason, String currentUserId);

    // UC40 / UC41 / UC42 extension UCs
    BookingResponse updateDishLines(UUID bookingId, EditBookingLinesRequest request, String currentUserId);
    BookingResponse updateServiceLines(UUID bookingId, EditBookingLinesRequest request, String currentUserId);
    BookingResponse updateBeverageLines(UUID bookingId, EditBookingLinesRequest request, String currentUserId);

    List<BookingResponse> getAllBookings();
    BookingResponse getBookingById(UUID bookingId);
}
