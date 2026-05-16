package com.wedding.management.domain.booking.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.booking.enums.BookingMode;
import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.enums.ManualMenuMode;
import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.menu.model.DishCombo;
import com.wedding.management.domain.shift.model.Shift;
import com.wedding.management.domain.weddingpackage.model.WeddingPackage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_date_shift", columnList = "booking_date, shift_id"),
        @Index(name = "idx_booking_hall", columnList = "hall_id"),
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_customer_phone", columnList = "customer_phone")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Booking extends BaseEntity {

    @Column(name = "booking_date", nullable = false)
    private Instant bookingDate;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "bride_name", nullable = false)
    private String brideName;

    @Column(name = "groom_name", nullable = false)
    private String groomName;

    @Column(name = "wedding_date", nullable = false)
    private Instant weddingDate;

    @Column(nullable = false)
    private Integer numberOfTables;

    @Column(nullable = false)
    private Integer numberOfReserveTables;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingMode bookingMode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "package_id")
    private WeddingPackage weddingPackage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "selected_menu_combo_id")
    private DishCombo selectedMenuCombo;

    @Column(nullable = false)
    private Double hallPrice;

    @Column(nullable = false)
    private Double subtotalAmount;

    @Column(nullable = false)
    private Double taxAmount;

    @Column(nullable = false)
    private Double bookingAmount;

    @Column(nullable = false)
    private Double depositAmount;

    @Column(nullable = false)
    private Double confirmedPaymentAmount;

    @Column(nullable = false)
    private Double remainingAmount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    private String cancelledBy;
    private Instant cancelledAt;

    private String deletedBy;
    private Instant deletedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookingLineSnapshot> lineSnapshots;

    @Enumerated(EnumType.STRING)
    @Column(name = "manual_menu_mode")
    private ManualMenuMode manualMenuMode;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookingMenuComboSnapshot> menuComboSnapshots;
}
