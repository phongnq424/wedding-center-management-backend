package com.wedding.management.domain.menu.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.menu.enums.DishStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Entity
@Table(name = "dishes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Dish extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "dish_type_id", nullable = false)
    private DishType dishType;

    @Column(nullable = false)
    private Double unitPrice;

    private String dishImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DishStatus status = DishStatus.ACTIVE;

    private String deletedBy;
    private Instant deletedAt;
}
