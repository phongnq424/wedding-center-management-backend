package com.wedding.management.domain.weddingpackage.dto;

import com.wedding.management.domain.weddingpackage.enums.WeddingPackageStatus;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WeddingPackageResponse {
    private UUID id;
    private String packageName;
    private String description;
    private UUID defaultMenuComboId;
    private String defaultMenuComboName;
    private List<UUID> menuComboOptions;
    private List<String> menuComboNames;
    private List<WeddingPackageServiceItemDTO> includedServiceList;
    private List<WeddingPackageBeverageAllowanceDTO> beverageAllowanceList;
    private List<WeddingPackageBenefitDTO> packageBenefitList;
    private List<WeddingPackageConditionDTO> conditionList;
    private String menuComboSummary;
    private String serviceSummary;
    private String beverageAllowanceSummary;
    private String conditionSummary;
    private Integer numberOfMenuCombos;
    private Integer numberOfIncludedServices;
    private Integer numberOfBeverageAllowances;
    private WeddingPackageStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
