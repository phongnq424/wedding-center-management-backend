package com.wedding.management.domain.staff.dto;

import com.wedding.management.domain.staff.enums.StaffStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StaffRequest {

    @NotBlank(message = "Họ tên nhân viên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private String description;

    @NotBlank(message = "Ảnh nhân viên không được để trống")
    private String staffImage;

    @NotNull(message = "Vai trò không được để trống")
    private UUID roleId;


    private StaffStatus status;

    /**
     * Used only for update staff.
     * BR-UST-3: VerifyCurrentPassword(currentUserId, currentPassword)
     */
    private String currentPassword;
}