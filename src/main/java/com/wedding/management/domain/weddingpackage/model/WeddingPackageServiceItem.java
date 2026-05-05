package com.wedding.management.domain.weddingpackage.model;

import com.wedding.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Entity
@Table(name = "wedding_package_services", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"package_id", "service_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class WeddingPackageServiceItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private WeddingPackage weddingPackage;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    private String serviceName;

    @Builder.Default
    private Integer quantity = 1;

    @Column(columnDefinition = "TEXT")
    private String note;
}
