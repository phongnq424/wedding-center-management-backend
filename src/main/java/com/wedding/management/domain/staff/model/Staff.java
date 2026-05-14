package com.wedding.management.domain.staff.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.staff.enums.StaffAccountStatus;
import com.wedding.management.domain.staff.enums.StaffStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "staff",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone_number")
        },
        indexes = {
                @Index(name = "idx_staff_email", columnList = "email"),
                @Index(name = "idx_staff_phone", columnList = "phone_number"),
                @Index(name = "idx_staff_role_id", columnList = "role_id"),
                @Index(name = "idx_staff_status", columnList = "status"),
                @Index(name = "idx_staff_account_status", columnList = "account_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class Staff extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "staff_image", nullable = false)
    private String staffImage;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StaffStatus status = StaffStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    @Builder.Default
    private StaffAccountStatus accountStatus = StaffAccountStatus.INACTIVE;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "activation_token")
    private String activationToken;

    @Column(name = "activation_token_created_at")
    private Instant activationTokenCreatedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
