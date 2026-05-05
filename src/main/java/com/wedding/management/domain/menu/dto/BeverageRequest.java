package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.BeverageStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
public class BeverageRequest {
    @NotBlank(message = "Tên thức uống không được để trống")
    private String name;
    @NotNull(message = "Loại thức uống không được để trống")
    private UUID beverageTypeId;
    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
    private Double unitPrice;
    private String beverageImage;
    private String description;
    private BeverageStatus status;
}
