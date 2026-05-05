package com.wedding.management.domain.staff.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteStaffRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;
}