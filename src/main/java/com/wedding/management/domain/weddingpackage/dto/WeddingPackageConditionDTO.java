package com.wedding.management.domain.weddingpackage.dto;

import com.wedding.management.domain.weddingpackage.enums.PackageConditionType;
import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeddingPackageConditionDTO {
    private UUID id;
    private PackageConditionType conditionType;
    private UUID hallTypeId;
    private String hallTypeName;
    private UUID shiftId;
    private String shiftName;
    private Integer numericValue;
    private String conditionValue;
    private Integer displayOrder;
}
