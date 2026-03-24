package com.wedding.management.domain.hall.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.hall.enums.HallStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "halls")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Hall extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;

    private Integer capacity;

    private Double basePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HallStatus status = HallStatus.INACTIVE;
}