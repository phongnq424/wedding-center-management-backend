package com.wedding.management.domain.menu.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.menu.enums.BeverageStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Entity
@Table(name = "beverages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Beverage extends BaseEntity {
    @Column( nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "beverage_type_id", nullable = false)
    private BeverageType beverageType;

    @Column(nullable = false)
    private Double unitPrice;

    private String beverageImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BeverageStatus status = BeverageStatus.ACTIVE;

    private String deletedBy;
    private Instant deletedAt;
}
