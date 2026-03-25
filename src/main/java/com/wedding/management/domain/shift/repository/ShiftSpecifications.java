package com.wedding.management.domain.shift.repository;

import com.wedding.management.domain.shift.model.Shift;
import com.wedding.management.domain.shift.enums.ShiftStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShiftSpecifications {
    public static Specification<Shift> filterShifts(String name, LocalTime fromTime, LocalTime toTime, ShiftStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (fromTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), fromTime));
            }
            if (toTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), toTime));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}