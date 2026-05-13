package com.wedding.management.domain.auth.service;

import com.wedding.management.common.mail.EmailService;
import com.wedding.management.domain.staff.model.Staff;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaService {

    private static final long MFA_CODE_DURATION_SECONDS = 5 * 60;
    private static final int OTP_BOUND = 1_000_000;

    private final Map<String, MfaChallenge> challenges = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailService emailService;

    public MfaService(EmailService emailService) {
        this.emailService = emailService;
    }

    public String createChallenge(Staff staff) {
        String challengeId = UUID.randomUUID().toString();
        String code = generateOtpCode();

        challenges.put(
                challengeId,
                new MfaChallenge(
                        staff.getId(),
                        code,
                        Instant.now().plusSeconds(MFA_CODE_DURATION_SECONDS)
                )
        );

        emailService.sendOtpEmail(staff.getEmail(), staff.getFullName(), code);

        return challengeId;
    }

    public UUID verify2FACode(String challengeId, String inputCode) {
        if (challengeId == null || challengeId.isBlank()
                || inputCode == null || inputCode.isBlank()) {
            return null;
        }

        MfaChallenge challenge = challenges.get(challengeId);

        if (challenge == null) {
            return null;
        }

        if (challenge.expiresAt().isBefore(Instant.now())) {
            challenges.remove(challengeId);
            return null;
        }

        if (!challenge.code().equals(inputCode.trim())) {
            return null;
        }

        challenges.remove(challengeId);
        return challenge.staffId();
    }

    private String generateOtpCode() {
        return String.format("%06d", secureRandom.nextInt(OTP_BOUND));
    }

    private record MfaChallenge(UUID staffId, String code, Instant expiresAt) {}
}