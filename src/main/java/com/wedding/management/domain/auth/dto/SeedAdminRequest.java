package com.wedding.management.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SeedAdminRequest {
    @NotBlank(message = "MSG2: Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "MSG2: Email không được để trống")
    private String email;

    @NotBlank(message = "MSG2: Số điện thoại không được để trống")
    private String phoneNumber;

    @NotBlank(message = "MSG2: Mật khẩu không được để trống")
    private String password;

    private String staffImage;
}
