package com.wedding.management.domain.weddingpackage.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.menu.model.DishCombo;
import com.wedding.management.domain.weddingpackage.enums.WeddingPackageStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "wedding_packages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class WeddingPackage extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "default_menu_combo_id", nullable = false)
    private DishCombo defaultMenuCombo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WeddingPackageStatus status = WeddingPackageStatus.ACTIVE;

    @OneToMany(mappedBy = "weddingPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeddingPackageMenuCombo> menuCombos;

    @OneToMany(mappedBy = "weddingPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeddingPackageServiceItem> serviceItems;

    @OneToMany(mappedBy = "weddingPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeddingPackageBeverageAllowance> beverageAllowances;

    @OneToMany(mappedBy = "weddingPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeddingPackageBenefit> benefits;

    @OneToMany(mappedBy = "weddingPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeddingPackageCondition> conditions;

    private String deletedBy;
    private Instant deletedAt;
}
