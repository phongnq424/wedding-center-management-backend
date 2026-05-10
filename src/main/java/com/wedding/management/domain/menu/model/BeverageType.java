package com.wedding.management.domain.menu.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Entity
@Table(name = "beverage_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class BeverageType extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BeverageTypeStatus status = BeverageTypeStatus.ACTIVE;

    private String deletedBy;
    private Instant deletedAt;
}
