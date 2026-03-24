package com.wedding.management.domain.hall.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class HallRequest {
    @NotBlank(message = "Tên sảnh không được để trống")
    private String name;

    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    @NotNull(message = "Giá cơ bản không được để trống")
    private Double basePrice;

    private String description;
}