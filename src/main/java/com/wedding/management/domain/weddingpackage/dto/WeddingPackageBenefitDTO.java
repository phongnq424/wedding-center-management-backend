package com.wedding.management.domain.weddingpackage.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeddingPackageBenefitDTO {
    private UUID id;
    private String benefitDescription;
    private Integer displayOrder;
}
