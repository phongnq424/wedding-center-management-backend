package com.wedding.management.domain.hall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class HallTypeRequest {
    @NotBlank(message = "Tên loại sảnh không được để trống")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá cơ sở phải lớn hơn 0")
    private Double basePrice;
}
