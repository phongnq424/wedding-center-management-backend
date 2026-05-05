package com.wedding.management.domain.booking.repository;

import com.wedding.management.domain.booking.enums.BookingHoldStatus;
import com.wedding.management.domain.booking.model.BookingHallHold;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingHallHoldRepository extends JpaRepository<BookingHallHold, UUID> {
    @Query("""
           SELECT h FROM BookingHallHold h
           WHERE h.hall.id = :hallId
           AND h.shift.id = :shiftId
           AND h.bookingDate = :bookingDate
           AND h.status = :status
           AND h.expiredAt > :now
           """)
    List<BookingHallHold> findActiveHoldForSlot(UUID hallId, UUID shiftId, Instant bookingDate, BookingHoldStatus status, Instant now);

    @Query("""
           SELECT h FROM BookingHallHold h
           WHERE h.hall.id = :hallId
           AND h.shift.id = :shiftId
           AND h.bookingDate = :bookingDate
           AND h.heldBy = :heldBy
           AND h.status = :status
           AND h.expiredAt > :now
           ORDER BY h.createdAt DESC
           """)
    Optional<BookingHallHold> findValidHoldByUser(UUID hallId, UUID shiftId, Instant bookingDate, String heldBy, BookingHoldStatus status, Instant now);

    @Modifying
    @Query("""
           UPDATE BookingHallHold h SET h.status = :newStatus
           WHERE h.hall.id = :hallId
           AND h.shift.id = :shiftId
           AND h.bookingDate = :bookingDate
           AND h.heldBy = :heldBy
           AND h.status = :oldStatus
           """)
    void updateHoldStatus(UUID hallId, UUID shiftId, Instant bookingDate, String heldBy, BookingHoldStatus oldStatus, BookingHoldStatus newStatus);
}
