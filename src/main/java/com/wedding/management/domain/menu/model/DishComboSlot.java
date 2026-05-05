package com.wedding.management.domain.menu.model;

import com.wedding.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dish_combo_slots", indexes = {
        @Index(name = "idx_combo_slot_combo_id", columnList = "dish_combo_id"),
        @Index(name = "idx_combo_slot_dish_id", columnList = "default_dish_id"),
        @Index(name = "idx_combo_slot_type_id", columnList = "dish_type_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class DishComboSlot extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_combo_id", nullable = false)
    private DishCombo dishCombo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "dish_type_id", nullable = false)
    private DishType dishType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "default_dish_id", nullable = false)
    private Dish defaultDish;

    @Column(nullable = false)
    private Boolean isReplaceable;

    @Column(nullable = false)
    private Integer displayOrder;
}
