package com.wedding.management.domain.booking.repository;

import com.wedding.management.domain.booking.model.BookingMenuComboSnapshot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingMenuComboSnapshotRepository extends JpaRepository<BookingMenuComboSnapshot, UUID> {

    @EntityGraph(attributePaths = "slotSnapshots")
    @Query("""
           SELECT DISTINCT c
           FROM BookingMenuComboSnapshot c
           WHERE c.booking.id = :bookingId
           ORDER BY c.displayOrder ASC
           """)
    List<BookingMenuComboSnapshot> findByBookingId(UUID bookingId);

    @Modifying
    @Query("DELETE FROM BookingMenuComboSnapshot c WHERE c.booking.id = :bookingId")
    void deleteByBookingId(UUID bookingId);
}