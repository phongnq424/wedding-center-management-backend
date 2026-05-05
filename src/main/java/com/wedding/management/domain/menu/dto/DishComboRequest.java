package com.wedding.management.domain.menu.dto;

import com.wedding.management.domain.menu.enums.DishComboStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class DishComboRequest {
    @NotBlank(message = "Tên combo món ăn không được để trống")
    private String name;
    @NotNull(message = "Tỷ lệ giảm giá combo không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tỷ lệ giảm giá phải lớn hơn 0")
    private Double comboDiscountRate;
    @NotEmpty(message = "Danh sách slot combo không được để trống")
    private List<DishComboSlotRequest> comboSlotList;
    private String description;
    private DishComboStatus status;
}
