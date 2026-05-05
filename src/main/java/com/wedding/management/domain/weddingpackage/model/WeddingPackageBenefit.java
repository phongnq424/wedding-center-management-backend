package com.wedding.management.domain.weddingpackage.model;

import com.wedding.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "wedding_package_benefits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class WeddingPackageBenefit extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private WeddingPackage weddingPackage;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String benefitDescription;

    @Column(nullable = false)
    private Integer displayOrder;
}
