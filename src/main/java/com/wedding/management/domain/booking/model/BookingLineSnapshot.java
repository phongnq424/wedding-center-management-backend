package com.wedding.management.domain.booking.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.booking.enums.BookingLineItemType;
import com.wedding.management.domain.booking.enums.BookingLineSourceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "booking_line_snapshots", indexes = {
        @Index(name = "idx_booking_line_booking", columnList = "booking_id"),
        @Index(name = "idx_booking_line_item_type", columnList = "item_type"),
        @Index(name = "idx_booking_line_item_id", columnList = "item_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class BookingLineSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private BookingLineItemType itemType;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Double discountAmount;

    @Column(nullable = false)
    private Double taxRate;

    @Column(nullable = false)
    private Double taxAmount;

    @Column(nullable = false)
    private Double lineAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingLineSourceType sourceType;

    private UUID sourceId;
    private String sourceName;

    @Column(nullable = false)
    private Boolean editable;

    @Column(nullable = false)
    private Boolean removable;

    @Column(nullable = false)
    private Integer displayOrder;
}
