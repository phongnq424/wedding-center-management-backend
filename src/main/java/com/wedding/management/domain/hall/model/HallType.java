package com.wedding.management.domain.hall.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.hall.enums.HallTypeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hall_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class HallType extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double basePrice;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HallTypeStatus status = HallTypeStatus.ACTIVE;
}
