package com.wedding.management.domain.hall.repository;

import com.wedding.management.domain.hall.enums.HallStatus;
import com.wedding.management.domain.hall.model.Hall;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class HallSpecifications {
    public static Specification<Hall> filterHalls(String name, Integer capacity, HallStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (capacity != null) {
                predicates.add(cb.ge(root.get("capacity"), capacity));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}