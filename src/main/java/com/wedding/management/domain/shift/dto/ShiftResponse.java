package com.wedding.management.domain.shift.dto;

import com.wedding.management.domain.shift.enums.ShiftStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Data @Builder
public class ShiftResponse {
    private UUID id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private ShiftStatus status;
    private Instant lastModifiedAt;
    private String lastModifiedBy;
}
