package com.wedding.management.domain.booking.repository;

import com.wedding.management.domain.booking.enums.BookingStatus;
import com.wedding.management.domain.booking.model.Booking;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT b FROM Booking b WHERE b.isDeleted = false ORDER BY b.updatedAt DESC")
    List<Booking> findAllActive();

    @Query("""
           SELECT b FROM Booking b
           WHERE b.bookingDate = :bookingDate
           AND b.shift.id = :shiftId
           AND b.isDeleted = false
           AND b.status IN :statuses
           """)
    List<Booking> findByBookingDateAndShiftAndStatuses(Instant bookingDate, UUID shiftId, List<BookingStatus> statuses);

    @Query("""
           SELECT COUNT(b) FROM Booking b
           WHERE b.hall.id = :hallId
           AND b.bookingDate = :bookingDate
           AND b.shift.id = :shiftId
           AND b.isDeleted = false
           AND b.status IN :statuses
           AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)
           """)
    long countSlotConflict(UUID hallId, Instant bookingDate, UUID shiftId, List<BookingStatus> statuses, UUID excludeBookingId);

    @Query("""
           SELECT COUNT(b) FROM Booking b
           WHERE b.shift.id = :shiftId
           AND b.bookingDate > :currentTime
           AND b.isDeleted = false
           AND b.status IN :statuses
           """)
    long countFutureBookingByShift(UUID shiftId, Instant currentTime, List<BookingStatus> statuses);
}
