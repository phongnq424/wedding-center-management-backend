package com.wedding.management.common.audit;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String action; // e.g., "CREATE_ROLE", "UPDATE_HALL", "DELETE_PERMISSION"

    @Column(nullable = false)
    private UUID targetId; // ID of the entity being audited

    private String targetName; // Name/identifier of the entity

    @Column(columnDefinition = "TEXT")
    private String details; // Additional details

    @Column(nullable = false)
    private Instant createdAt;
}
