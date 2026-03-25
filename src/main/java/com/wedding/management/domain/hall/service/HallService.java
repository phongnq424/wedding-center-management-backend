package com.wedding.management.domain.hall.service;

import com.wedding.management.domain.hall.dto.HallRequest;
import com.wedding.management.domain.hall.dto.HallResponse;
import java.util.List;
import java.util.UUID;

public interface HallService {
    HallResponse createHall(HallRequest request);
    HallResponse updateHall(UUID id, HallRequest request);
    List<HallResponse> searchHalls(String name, Integer capacity, String status);
    void deleteHall(UUID id);
    HallResponse activateHall(UUID id);
}