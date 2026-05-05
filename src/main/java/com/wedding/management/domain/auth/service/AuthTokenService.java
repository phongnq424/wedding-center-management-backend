package com.wedding.management.domain.auth.service;

import com.wedding.management.domain.auth.model.AuthSession;
import com.wedding.management.domain.auth.repository.AuthSessionRepository;
import com.wedding.management.domain.staff.model.Staff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthTokenService {

    private static final long SESSION_DURATION_SECONDS = 8 * 60 * 60;
    private final SecureRandom secureRandom = new SecureRandom();
    private final AuthSessionRepository authSessionRepository;

    public AuthTokenService(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    public TokenPair createSession(Staff staff, String createdBy) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(SESSION_DURATION_SECONDS);

        AuthSession session = AuthSession.builder()
                .tokenHash(tokenHash)
                .staff(staff)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .createdAt(now)
                .createdBy(createdBy)
                .isDeleted(false)
                .build();

        authSessionRepository.save(session);
        return new TokenPair(rawToken, expiresAt);
    }

    @Transactional(readOnly = true)
    public Optional<AuthSession> findValidSessionByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return authSessionRepository.findValidSession(hashToken(rawToken), Instant.now());
    }

    public void revokeRawToken(String rawToken, String revokedBy, String reason) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        authSessionRepository.revokeByTokenHash(hashToken(rawToken), Instant.now(), revokedBy, reason);
    }

    public void revokeAllSessions(UUID staffId, String revokedBy, String reason) {
        authSessionRepository.revokeAllByStaffId(staffId, Instant.now(), revokedBy, reason);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record TokenPair(String token, Instant expiresAt) {}
}
