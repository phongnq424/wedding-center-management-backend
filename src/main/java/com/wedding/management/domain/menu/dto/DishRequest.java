package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.DishStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Data
public class DishRequest {
    @NotBlank(message = "Tên món ăn không được để trống")
    private String name;
    @NotNull(message = "Loại món ăn không được để trống")
    private UUID dishTypeId;
    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
    private Double unitPrice;
    private MultipartFile dishImage;
    private String description;
    private DishStatus status;
}
