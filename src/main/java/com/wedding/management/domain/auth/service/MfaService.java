package com.wedding.management.domain.auth.service;

import com.wedding.management.domain.staff.model.Staff;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaService {

    private final Map<String, MfaChallenge> challenges = new ConcurrentHashMap<>();

    public String createChallenge(Staff staff) {
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new MfaChallenge(staff.getId(), "123456", Instant.now().plusSeconds(300)));
        System.out.println("2FA code for " + staff.getEmail() + " = 123456");
        return challengeId;
    }

    public UUID verify2FACode(String challengeId, String inputCode) {
        MfaChallenge challenge = challenges.get(challengeId);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())) {
            return null;
        }

        if (!challenge.code().equals(inputCode)) {
            return null;
        }

        challenges.remove(challengeId);
        return challenge.staffId();
    }

    private record MfaChallenge(UUID staffId, String code, Instant expiresAt) {}
}
