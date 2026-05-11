package com.wedding.management.domain.payment.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.booking.model.Booking;
import com.wedding.management.domain.payment.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.time.*;

@Entity
@Table(name="payments", indexes={
        @Index(name="idx_payment_booking", columnList="booking_id"),
        @Index(name="idx_payment_status", columnList="status"),
        @Index(name="idx_payment_type", columnList="payment_type"),
        @Index(name="idx_payment_date", columnList="payment_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain=true)
public class Payment extends BaseEntity {
    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="booking_id", nullable=false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name="payment_type", nullable=false)
    private PaymentType paymentType;

    @Column(nullable=false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name="payment_method")
    private PaymentMethod paymentMethod;

    @Column(name="payment_date")
    private LocalDate paymentDate;

    @Column(name="received_amount")
    private Double receivedAmount;

    @Column(name="change_amount")
    private Double changeAmount;

    @Column(name="reference_number")
    private String referenceNumber;

    @Column(columnDefinition="TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPROCESSED;

    @Column(columnDefinition="TEXT")
    private String cancelReason;
    private String cancelledBy;
    private Instant cancelledAt;
    private String processedBy;
    private Instant processedAt;

    @Column(columnDefinition="TEXT")
    private String failureReason;
}
