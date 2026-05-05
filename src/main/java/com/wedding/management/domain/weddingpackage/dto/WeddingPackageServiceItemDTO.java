package com.wedding.management.domain.weddingpackage.dto;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeddingPackageServiceItemDTO {
    private UUID id;
    private UUID serviceId;
    private String serviceName;
    private Integer quantity;
    private String note;
}
