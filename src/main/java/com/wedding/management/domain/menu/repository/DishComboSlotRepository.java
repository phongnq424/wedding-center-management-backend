package com.wedding.management.domain.menu.repository;

import com.wedding.management.domain.menu.model.DishComboSlot;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DishComboSlotRepository extends JpaRepository<DishComboSlot, UUID> {
    @Query("SELECT s FROM DishComboSlot s WHERE s.dishCombo.id = :comboId ORDER BY s.displayOrder ASC")
    List<DishComboSlot> findByComboId(UUID comboId);

    @Modifying
    @Query("DELETE FROM DishComboSlot s WHERE s.dishCombo.id = :comboId")
    void deleteByComboId(UUID comboId);
}
