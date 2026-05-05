package com.wedding.management.domain.booking.model;

import com.wedding.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "booking_package_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class BookingPackageSnapshot extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "package_id")
    private UUID packageId;

    private String packageName;

    @Column(columnDefinition = "TEXT")
    private String packageDescription;

    @Column(columnDefinition = "TEXT")
    private String packagePolicySnapshot;

    @Column(name = "selected_menu_combo_id")
    private UUID selectedMenuComboId;

    private String selectedMenuComboName;
}
