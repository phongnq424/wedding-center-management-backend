package com.wedding.management.domain.weddingpackage.dto;

import com.wedding.management.domain.weddingpackage.enums.PackageBenefitItemType;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeddingPackageBenefitDTO {
    private UUID id;

    private PackageBenefitItemType itemType; // DISH, SERVICE, BEVERAGE
    private UUID itemId;
    private String itemName;

    private Integer quantity;

    // Giá trị thật của món/dịch vụ/nước, chỉ để hiển thị "quà tặng trị giá..."
    private Double unitValue;
    private Double totalValue;

    // Khách trả cho benefit luôn là 0
    private Double customerPayAmount;

    private String note;
    private Integer displayOrder;
}