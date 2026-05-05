package com.wedding.management.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "MSG2: Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @NotBlank(message = "MSG2: Mật khẩu mới không được để trống")
    private String newPassword;

    @NotBlank(message = "MSG2: Xác nhận mật khẩu mới không được để trống")
    private String confirmNewPassword;
}
