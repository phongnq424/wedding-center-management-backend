package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.BeverageTypeStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BeverageTypeRequest {
    @NotBlank(message = "Tên loại thức uống không được để trống")
    private String name;
    private String description;
    private BeverageTypeStatus status;
}
