package com.wedding.management.domain.booking.repository;

import com.wedding.management.domain.booking.enums.BookingLineItemType;
import com.wedding.management.domain.booking.enums.BookingLineSourceType;
import com.wedding.management.domain.booking.model.BookingLineSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingLineSnapshotRepository extends JpaRepository<BookingLineSnapshot, UUID> {

    @Query("SELECT l FROM BookingLineSnapshot l WHERE l.booking.id = :bookingId ORDER BY l.displayOrder ASC")
    List<BookingLineSnapshot> findByBookingId(UUID bookingId);

    @Query("""
           SELECT l
           FROM BookingLineSnapshot l
           WHERE l.booking.id = :bookingId
           AND l.itemType = :itemType
           ORDER BY l.displayOrder ASC
           """)
    List<BookingLineSnapshot> findByBookingIdAndItemType(UUID bookingId, BookingLineItemType itemType);

    @Query("""
           SELECT l
           FROM BookingLineSnapshot l
           WHERE l.booking.id = :bookingId
           AND l.sourceType = :sourceType
           ORDER BY l.displayOrder ASC
           """)
    List<BookingLineSnapshot> findByBookingIdAndSourceType(UUID bookingId, BookingLineSourceType sourceType);

    @Modifying
    @Query("DELETE FROM BookingLineSnapshot l WHERE l.booking.id = :bookingId")
    void deleteByBookingId(UUID bookingId);

    @Modifying
    @Query("DELETE FROM BookingLineSnapshot l WHERE l.booking.id = :bookingId AND l.itemType = :itemType")
    void deleteByBookingIdAndItemType(UUID bookingId, BookingLineItemType itemType);

    @Modifying
    @Query("DELETE FROM BookingLineSnapshot l WHERE l.booking.id = :bookingId AND l.sourceType = :sourceType")
    void deleteByBookingIdAndSourceType(UUID bookingId, BookingLineSourceType sourceType);
}