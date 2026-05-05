package com.wedding.management.domain.hall.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.hall.enums.TimeSlot;
import com.wedding.management.domain.hall.enums.DayType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hall_pricing", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"hall_id", "time_slot", "day_type"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class HallPricing extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayType dayType;

    @Column(nullable = false)
    private Double price;
}
