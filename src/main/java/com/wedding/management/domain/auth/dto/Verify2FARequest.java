package com.wedding.management.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Verify2FARequest {
    @NotBlank(message = "MSG2: Mã 2FA không được để trống")
    private String mfaChallengeId;

    @NotBlank(message = "MSG2: Mã 2FA không được để trống")
    private String inputCode;
}
