package com.wedding.management.domain.booking.dto;

import com.wedding.management.domain.booking.enums.BookingStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingSearchCriteria {
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String brideName;
    private String groomName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bookingDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bookingDateTo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate weddingDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate weddingDateTo;

    private UUID hallId;
    private UUID shiftId;
    private BookingStatus status;
}
