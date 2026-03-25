package com.wedding.management.domain.service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ServiceRequest {
    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String name;

    @NotNull(message = "Giá dịch vụ không được để trống")
    @Positive(message = "Giá dịch vụ phải lớn hơn 0")
    private Double price;

    private String description;
}