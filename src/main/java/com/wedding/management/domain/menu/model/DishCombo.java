package com.wedding.management.domain.menu.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.menu.enums.DishComboStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "dish_combos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class DishCombo extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double comboDiscountRate;

    @Column(nullable = false)
    private Double estimatedOriginalPricePerTable;

    @Column(nullable = false)
    private Double estimatedComboPricePerTable;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DishComboStatus status = DishComboStatus.ACTIVE;

    @OneToMany(mappedBy = "dishCombo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DishComboSlot> slots;

    private String deletedBy;
    private Instant deletedAt;
}
