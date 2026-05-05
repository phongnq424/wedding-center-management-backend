package com.wedding.management.domain.booking.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.booking.enums.BookingHoldStatus;
import com.wedding.management.domain.hall.model.Hall;
import com.wedding.management.domain.shift.model.Shift;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "booking_hall_holds", indexes = {
        @Index(name = "idx_hold_slot", columnList = "hall_id, shift_id, booking_date"),
        @Index(name = "idx_hold_status", columnList = "status"),
        @Index(name = "idx_hold_user", columnList = "held_by")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class BookingHallHold extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "booking_date", nullable = false)
    private Instant bookingDate;

    @Column(name = "held_by", nullable = false)
    private String heldBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingHoldStatus status;

    @Column(nullable = false)
    private Instant expiredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
