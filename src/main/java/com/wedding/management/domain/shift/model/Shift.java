package com.wedding.management.domain.shift.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.shift.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Entity
@Table(name = "shifts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Shift extends BaseEntity {

    @Column(nullable = false)
    private String name; // Ví dụ: Ca Sáng, Ca Chiều, Ca Tối

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ShiftStatus status = ShiftStatus.ACTIVE;
}