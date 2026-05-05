package com.wedding.management.domain.weddingpackage.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeddingPackageBeverageAllowanceDTO {
    private UUID id;
    private UUID beverageId;
    private String beverageName;
    private Integer allowanceQuantity;
    private String note;
}
