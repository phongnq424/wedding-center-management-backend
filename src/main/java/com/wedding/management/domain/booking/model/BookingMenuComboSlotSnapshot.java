package com.wedding.management.domain.booking.model;

import com.wedding.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "booking_menu_combo_slot_snapshots", indexes = {
        @Index(name = "idx_booking_combo_slot_snapshot_combo", columnList = "combo_snapshot_id"),
        @Index(name = "idx_booking_combo_slot_snapshot_selected_dish", columnList = "selected_dish_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class BookingMenuComboSlotSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "combo_snapshot_id", nullable = false)
    private BookingMenuComboSnapshot comboSnapshot;

    @Column(name = "slot_id")
    private UUID slotId;

    @Column(name = "slot_name")
    private String slotName;

    @Column(name = "original_dish_id")
    private UUID originalDishId;

    @Column(name = "original_dish_name")
    private String originalDishName;

    @Column(name = "original_dish_price", nullable = false)
    private Double originalDishPrice;

    @Column(name = "selected_dish_id", nullable = false)
    private UUID selectedDishId;

    @Column(name = "selected_dish_name", nullable = false)
    private String selectedDishName;

    @Column(name = "selected_dish_price", nullable = false)
    private Double selectedDishPrice;

    @Column(name = "replaced", nullable = false)
    private Boolean replaced;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}