package com.wedding.management.domain.booking.repository;

import com.wedding.management.domain.booking.model.BookingMenuComboSnapshot;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingMenuComboSnapshotRepository extends JpaRepository<BookingMenuComboSnapshot, UUID> {

    @Query("SELECT c FROM BookingMenuComboSnapshot c WHERE c.booking.id = :bookingId ORDER BY c.displayOrder ASC")
    List<BookingMenuComboSnapshot> findByBookingId(UUID bookingId);

    @Modifying
    @Query("DELETE FROM BookingMenuComboSnapshot c WHERE c.booking.id = :bookingId")
    void deleteByBookingId(UUID bookingId);
}