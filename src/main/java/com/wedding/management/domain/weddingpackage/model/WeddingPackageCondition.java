package com.wedding.management.domain.weddingpackage.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.hall.model.HallType;
import com.wedding.management.domain.weddingpackage.enums.PackageConditionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Entity
@Table(name = "wedding_package_conditions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class WeddingPackageCondition extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private WeddingPackage weddingPackage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageConditionType conditionType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hall_type_id")
    private HallType hallType;

    @Column(name = "shift_id")
    private UUID shiftId;

    private String shiftName;

    private Integer numericValue;

    @Column(columnDefinition = "TEXT")
    private String conditionValue;

    @Column(nullable = false)
    private Integer displayOrder;
}
