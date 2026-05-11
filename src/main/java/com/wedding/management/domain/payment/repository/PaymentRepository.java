package com.wedding.management.domain.payment.repository;

import com.wedding.management.domain.payment.enums.*;
import com.wedding.management.domain.payment.model.Payment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Payment> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT p FROM Payment p WHERE p.isDeleted = false ORDER BY p.updatedAt DESC")
    List<Payment> findAllActive();

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.isDeleted = false ORDER BY p.updatedAt DESC")
    List<Payment> findByBookingId(UUID bookingId);

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.status = :status AND p.isDeleted = false")
    List<Payment> findByBookingIdAndStatus(UUID bookingId, PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.booking.id = :bookingId AND p.paymentType = :paymentType AND p.status = :status AND p.isDeleted = false AND (:excludePaymentId IS NULL OR p.id <> :excludePaymentId)")
    long countByBookingAndTypeAndStatus(UUID bookingId, PaymentType paymentType, PaymentStatus status, UUID excludePaymentId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.booking.id = :bookingId AND p.paymentType = :paymentType AND p.status = :status AND p.isDeleted = false")
    long countByBookingAndTypeAndStatus(UUID bookingId, PaymentType paymentType, PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.booking.id = :bookingId AND p.status = :status AND p.isDeleted = false")
    Double sumAmountByBookingAndStatus(UUID bookingId, PaymentStatus status);

    @Query("""
       SELECT COALESCE(SUM(p.amount), 0)
       FROM Payment p
       WHERE p.booking.id = :bookingId
       AND p.status = :status
       AND p.isDeleted = false
       """)
    double sumAmountByBookingIdAndStatus(UUID bookingId, PaymentStatus status);
}
