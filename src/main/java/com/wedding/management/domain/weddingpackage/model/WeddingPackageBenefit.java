package com.wedding.management.domain.weddingpackage.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.weddingpackage.enums.PackageBenefitItemType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "wedding_package_benefits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class WeddingPackageBenefit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private WeddingPackage weddingPackage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageBenefitItemType itemType;

    @Column(nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitValue;

    @Column(nullable = false)
    private Double totalValue;

    @Column(nullable = false)
    private Double customerPayAmount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private Integer displayOrder;
}