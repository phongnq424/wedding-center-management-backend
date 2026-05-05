package com.wedding.management.domain.hall.dto;

import com.wedding.management.domain.hall.enums.TimeSlot;
import com.wedding.management.domain.hall.enums.DayType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class HallPricingDTO {
    private UUID id;
    private TimeSlot timeSlot;
    private DayType dayType;
    private Double price;
}
