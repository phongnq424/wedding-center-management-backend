package com.wedding.management.domain.hall.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
public class HallRequest {
    @NotBlank(message = "Tên sảnh không được để trống")
    private String name;

    @NotNull(message = "ID loại sảnh không được để trống")
    private UUID hallTypeId;

    @NotNull(message = "Số bàn tối thiểu không được để trống")
    @Min(value = 1, message = "Số bàn tối thiểu phải lớn hơn 0")
    private Integer minTables;

    @NotNull(message = "Số bàn tối đa không được để trống")
    @Min(value = 1, message = "Số bàn tối đa phải lớn hơn 0")
    private Integer maxTables;

    private MultipartFile hallImage;

    private String description;

    // Pricing matrix: List of 6 prices (3 time slots × 2 day types)
    // Order: MORNING-WEEKDAY, MORNING-WEEKEND, AFTERNOON-WEEKDAY, AFTERNOON-WEEKEND, EVENING-WEEKDAY, EVENING-WEEKEND
    private List<HallPricingDTO> pricings;
}
