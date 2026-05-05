package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.DishTypeStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DishTypeRequest {
    @NotBlank(message = "Tên loại món ăn không được để trống")
    private String name;
    private String description;
    private DishTypeStatus status;
}
