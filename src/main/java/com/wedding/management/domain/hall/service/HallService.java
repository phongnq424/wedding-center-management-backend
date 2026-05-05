package com.wedding.management.domain.hall.service;

import com.wedding.management.domain.hall.dto.HallRequest;
import com.wedding.management.domain.hall.dto.HallResponse;
import com.wedding.management.domain.hall.enums.HallStatus;
import java.util.List;
import java.util.UUID;

public interface HallService {
    // UC19: Add New Hall
    HallResponse createHall(HallRequest request, String currentUserId);

    // UC20: Update Hall
    HallResponse updateHall(UUID hallId, HallRequest request, String currentUserId, long lastModifiedAt);

    // UC21: Search Hall
    List<HallResponse> searchHalls(String hallName, UUID hallTypeId, Integer minTablesFrom, Integer maxTablesTo, HallStatus status);

    // UC22: Delete Hall
    void deleteHall(UUID hallId, String currentUserId);

    // Helper methods
    List<HallResponse> getAllHalls();
    HallResponse getHallById(UUID hallId);
}
