package com.wedding.management.domain.invoice.repository;

import com.wedding.management.domain.invoice.enums.*;
import com.wedding.management.domain.invoice.model.Invoice;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.isDeleted = false")
    Optional<Invoice> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT i FROM Invoice i WHERE i.isDeleted = false ORDER BY i.updatedAt DESC")
    List<Invoice> findAllActive();

    @Query("SELECT i FROM Invoice i WHERE i.booking.id = :bookingId AND i.isDeleted = false ORDER BY i.updatedAt DESC")
    List<Invoice> findByBookingId(UUID bookingId);

    @Query("SELECT i FROM Invoice i WHERE i.booking.id = :bookingId AND i.status IN :statuses AND i.isDeleted = false")
    List<Invoice> findActiveByBookingAndStatuses(UUID bookingId, Collection<InvoiceStatus> statuses);
}
