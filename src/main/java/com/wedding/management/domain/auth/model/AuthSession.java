package com.wedding.management.domain.auth.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.staff.model.Staff;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "auth_sessions", indexes = {
        @Index(name = "idx_auth_session_token_hash", columnList = "token_hash"),
        @Index(name = "idx_auth_session_staff", columnList = "staff_id"),
        @Index(name = "idx_auth_session_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class AuthSession extends BaseEntity {

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by")
    private String revokedBy;

    @Column(name = "revoked_reason")
    private String revokedReason;
}
