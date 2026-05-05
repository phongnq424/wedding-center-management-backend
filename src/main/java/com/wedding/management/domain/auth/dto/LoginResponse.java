package com.wedding.management.domain.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LoginResponse {
    private UUID staffId;
    private String fullName;
    private String email;
    private UUID roleId;
    private String roleName;
    private boolean requires2FA;
    private String mfaChallengeId;
    private String accessToken;
    private Instant expiresAt;
}
