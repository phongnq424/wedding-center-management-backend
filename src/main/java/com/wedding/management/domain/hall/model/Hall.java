package com.wedding.management.domain.hall.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.hall.enums.HallStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Entity
@Table(name = "halls")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Hall extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "hall_type_id", nullable = false)
    private HallType hallType;

    @Column(nullable = false)
    private Integer minTables;

    @Column(nullable = false)
    private Integer maxTables;

    private String hallImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HallPricing> pricings;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HallStatus status = HallStatus.INACTIVE;
}