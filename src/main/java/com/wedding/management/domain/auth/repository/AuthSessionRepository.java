package com.wedding.management.domain.auth.repository;

import com.wedding.management.domain.auth.model.AuthSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByTokenHash(String tokenHash);

    @Query("""
           SELECT s FROM AuthSession s
           WHERE s.tokenHash = :tokenHash
           AND s.revokedAt IS NULL
           AND s.expiresAt > :now
           """)
    Optional<AuthSession> findValidSession(String tokenHash, Instant now);

    @Modifying
    @Query("""
           UPDATE AuthSession s
           SET s.revokedAt = :revokedAt,
               s.revokedBy = :revokedBy,
               s.revokedReason = :reason,
               s.updatedAt = :revokedAt,
               s.updatedBy = :revokedBy
           WHERE s.staff.id = :staffId
           AND s.revokedAt IS NULL
           """)
    int revokeAllByStaffId(UUID staffId, Instant revokedAt, String revokedBy, String reason);

    @Modifying
    @Query("""
           UPDATE AuthSession s
           SET s.revokedAt = :revokedAt,
               s.revokedBy = :revokedBy,
               s.revokedReason = :reason,
               s.updatedAt = :revokedAt,
               s.updatedBy = :revokedBy
           WHERE s.tokenHash = :tokenHash
           AND s.revokedAt IS NULL
           """)
    int revokeByTokenHash(String tokenHash, Instant revokedAt, String revokedBy, String reason);
}
