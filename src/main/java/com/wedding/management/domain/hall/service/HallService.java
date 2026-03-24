package com.wedding.management.domain.hall.service;

import com.wedding.management.domain.hall.dto.HallRequest;
import com.wedding.management.domain.hall.dto.HallResponse;
import java.util.List;

public interface HallService {
    HallResponse createHall(HallRequest request);
    HallResponse updateHall(Long id, HallRequest request);
    List<HallResponse> searchHalls(String name, Integer capacity, String status);
    void deleteHall(Long id);
    HallResponse activateHall(Long id);
}