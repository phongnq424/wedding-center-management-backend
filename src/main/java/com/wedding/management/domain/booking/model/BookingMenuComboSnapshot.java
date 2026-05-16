package com.wedding.management.domain.booking.model;

import com.wedding.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "booking_menu_combo_snapshots", indexes = {
        @Index(name = "idx_booking_combo_snapshot_booking", columnList = "booking_id"),
        @Index(name = "idx_booking_combo_snapshot_combo", columnList = "combo_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class BookingMenuComboSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "combo_id", nullable = false)
    private UUID comboId;

    @Column(name = "combo_name", nullable = false)
    private String comboName;

    @Column(name = "table_count", nullable = false)
    private Integer tableCount;

    @Column(name = "original_combo_price", nullable = false)
    private Double originalComboPrice;

    @Column(name = "discount_rate", nullable = false)
    private Double discountRate;

    @Column(name = "discounted_combo_price", nullable = false)
    private Double discountedComboPrice;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @OneToMany(mappedBy = "comboSnapshot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookingMenuComboSlotSnapshot> slotSnapshots;
}