package com.wedding.management.domain.booking.dto;

import com.wedding.management.domain.booking.enums.BookingMode;
import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.enums.ManualMenuMode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class BookingResponse {
    private UUID id;
    private LocalDate bookingDate;
    private UUID shiftId;
    private String shiftName;
    private UUID hallId;
    private String hallName;
    private String hallTypeName;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String brideName;
    private String groomName;
    private LocalDate weddingDate;
    private Integer numberOfTables;
    private Integer numberOfReserveTables;
    private BookingMode bookingMode;
    private UUID packageId;
    private String packageName;
    private UUID selectedMenuComboId;
    private String selectedMenuComboName;
    private Double hallPrice;
    private Double subtotalAmount;
    private Double taxAmount;
    private Double bookingAmount;
    private Double depositAmount;
    private Double confirmedPaymentAmount;
    private Double remainingAmount;
    private String note;
    private BookingStatus status;
    private String cancelReason;
    private List<BookingLineResponse> bookingLines;
    private ManualMenuMode manualMenuMode;
    private List<BookingMenuComboSnapshotResponse> menuComboSnapshots;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
