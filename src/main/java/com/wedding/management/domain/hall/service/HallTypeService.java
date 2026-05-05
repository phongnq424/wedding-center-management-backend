package com.wedding.management.domain.hall.service;

import com.wedding.management.domain.hall.dto.HallTypeRequest;
import com.wedding.management.domain.hall.dto.HallTypeResponse;
import com.wedding.management.domain.hall.enums.HallTypeStatus;
import java.util.List;
import java.util.UUID;

public interface HallTypeService {
    // UC15: Add New Hall Type
    HallTypeResponse createHallType(HallTypeRequest request, String currentUserId);

    // UC16: Update Hall Type
    HallTypeResponse updateHallType(UUID hallTypeId, HallTypeRequest request, String currentUserId, long lastModifiedAt);

    // UC17: Search Hall Type
    List<HallTypeResponse> searchHallTypes(String nameKeyword, Double minBasePrice, HallTypeStatus status);

    // UC18: Delete Hall Type
    void deleteHallType(UUID hallTypeId, String currentUserId);

    // Helper methods
    List<HallTypeResponse> getAllHallTypes();
    HallTypeResponse getHallTypeById(UUID hallTypeId);
}
