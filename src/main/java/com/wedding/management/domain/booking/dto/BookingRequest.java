package com.wedding.management.domain.booking.dto;

import com.wedding.management.domain.booking.enums.BookingMode;
import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.enums.ManualMenuMode;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BookingRequest {
    @NotNull(message = "Ngày đặt tiệc không được để trống")
    private LocalDate bookingDate;

    @NotNull(message = "Ca không được để trống")
    private UUID shiftId;

    @NotNull(message = "Sảnh không được để trống")
    private UUID hallId;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại khách hàng không được để trống")
    private String customerPhone;

    private String customerEmail;

    @NotBlank(message = "Tên cô dâu không được để trống")
    private String brideName;

    @NotBlank(message = "Tên chú rể không được để trống")
    private String groomName;

    @NotNull(message = "Ngày cưới không được để trống")
    private LocalDate weddingDate;

    @NotNull(message = "Số bàn không được để trống")
    private Integer numberOfTables;

    @NotNull(message = "Số bàn dự phòng không được để trống")
    private Integer numberOfReserveTables;

    @NotNull(message = "Chế độ đặt tiệc không được để trống")
    private BookingMode bookingMode;

    private UUID packageId;
    private UUID selectedMenuComboId;
    private List<BookingLineRequest> bookingDraftLines;

    private Integer softDrinkQuantity;
    private Integer beerQuantity;
    private Double depositAmount;
    private String note;
    private BookingStatus status;
    private ManualMenuMode manualMenuMode;
    private List<BookingMenuComboRequest> manualComboSelections;
}
