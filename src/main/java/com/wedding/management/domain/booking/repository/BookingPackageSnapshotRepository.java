package com.wedding.management.domain.booking.repository;

import com.wedding.management.domain.booking.model.BookingPackageSnapshot;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingPackageSnapshotRepository extends JpaRepository<BookingPackageSnapshot, UUID> {
    @Query("SELECT p FROM BookingPackageSnapshot p WHERE p.booking.id = :bookingId")
    Optional<BookingPackageSnapshot> findByBookingId(UUID bookingId);

    @Modifying
    @Query("DELETE FROM BookingPackageSnapshot p WHERE p.booking.id = :bookingId")
    void deleteByBookingId(UUID bookingId);
}
