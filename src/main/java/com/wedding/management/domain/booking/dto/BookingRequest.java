package com.wedding.management.domain.booking.dto;

import com.wedding.management.domain.booking.enums.BookingMode;
import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.enums.ManualMenuMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /**
     * PACKAGE mode only.
     */
    private UUID packageId;

    /**
     * PACKAGE mode only.
     */
    private UUID selectedMenuComboId;

    /**
     * PACKAGE:
     * - giữ nguyên logic cũ của package.
     *
     * MANUAL + CUSTOM:
     * - chứa DISH, SERVICE, BEVERAGE, CUSTOM.
     *
     * MANUAL + COMBO:
     * - chỉ nên chứa SERVICE, BEVERAGE, CUSTOM.
     * - DISH sẽ được backend sinh từ manualComboSelections để tránh duplicate và để reconstruct combo đúng.
     */
    @Valid
    private List<BookingLineRequest> bookingDraftLines;

    private Integer softDrinkQuantity;

    private Integer beerQuantity;

    private Double depositAmount;

    private String note;

    private BookingStatus status;

    /**
     * MANUAL mode only.
     * COMBO: chọn combo món ăn.
     * CUSTOM: tự chọn từng món.
     */
    private ManualMenuMode manualMenuMode;

    /**
     * MANUAL + COMBO only.
     */
    @Valid
    private List<BookingMenuComboRequest> manualComboSelections;
}