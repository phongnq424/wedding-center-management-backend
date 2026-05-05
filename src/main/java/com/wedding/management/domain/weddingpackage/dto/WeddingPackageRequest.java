package com.wedding.management.domain.weddingpackage.dto;

import com.wedding.management.domain.weddingpackage.enums.WeddingPackageStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.*;

@Data
public class WeddingPackageRequest {
    @NotBlank(message = "Tên gói tiệc không được để trống")
    private String packageName;

    private String description;

    private List<UUID> menuComboOptions;
    private UUID defaultMenuComboId;
    private List<WeddingPackageServiceItemDTO> includedServiceList;
    private List<WeddingPackageBeverageAllowanceDTO> beverageAllowanceList;
    private List<WeddingPackageBenefitDTO> packageBenefitList;
    private List<WeddingPackageConditionDTO> conditionList;
    private WeddingPackageStatus status;
}
