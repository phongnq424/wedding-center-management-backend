package com.wedding.management.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "MSG2: Email không được để trống")
    private String email;

    @NotBlank(message = "MSG2: Mật khẩu không được để trống")
    private String password;
}
