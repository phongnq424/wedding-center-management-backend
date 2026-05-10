package com.wedding.management.domain.service.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.service.enums.ServiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Entity
@Table(name = "services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Service extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    private String serviceImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.ACTIVE;

    private String deletedBy;
    private Instant deletedAt;
}
