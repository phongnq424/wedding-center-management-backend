package com.wedding.management.domain.invoice.repository;

import com.wedding.management.domain.invoice.model.InvoiceLineSnapshot;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface InvoiceLineSnapshotRepository extends JpaRepository<InvoiceLineSnapshot, UUID> {
    @Query("SELECT l FROM InvoiceLineSnapshot l WHERE l.invoice.id = :invoiceId ORDER BY l.displayOrder ASC")
    List<InvoiceLineSnapshot> findByInvoiceId(UUID invoiceId);
}
