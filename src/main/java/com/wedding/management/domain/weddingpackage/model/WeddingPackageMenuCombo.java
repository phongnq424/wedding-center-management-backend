package com.wedding.management.domain.weddingpackage.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.menu.model.DishCombo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "wedding_package_menu_combos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"package_id", "dish_combo_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class WeddingPackageMenuCombo extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private WeddingPackage weddingPackage;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "dish_combo_id", nullable = false)
    private DishCombo dishCombo;
}
