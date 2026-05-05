package com.wedding.management.domain.hall.repository;

import com.wedding.management.domain.hall.model.HallPricing;
import com.wedding.management.domain.hall.enums.TimeSlot;
import com.wedding.management.domain.hall.enums.DayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HallPricingRepository extends JpaRepository<HallPricing, UUID> {
    @Query("SELECT hp FROM HallPricing hp WHERE hp.hall.id = :hallId")
    List<HallPricing> findByHallId(UUID hallId);

    @Query("SELECT hp FROM HallPricing hp WHERE hp.hall.id = :hallId AND hp.timeSlot = :timeSlot AND hp.dayType = :dayType")
    Optional<HallPricing> findByHallIdAndTimeSlotAndDayType(UUID hallId, TimeSlot timeSlot, DayType dayType);
}
