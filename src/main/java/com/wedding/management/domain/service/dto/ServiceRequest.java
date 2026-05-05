package com.wedding.management.domain.service.dto;

import com.wedding.management.domain.service.enums.ServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ServiceRequest {
    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String name;

    @NotNull(message = "Giá dịch vụ không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá dịch vụ phải lớn hơn 0")
    private Double price;

    private MultipartFile serviceImage;
    private String description;
    private ServiceStatus status;
}
