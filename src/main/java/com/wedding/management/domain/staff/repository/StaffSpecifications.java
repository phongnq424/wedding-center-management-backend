package com.wedding.management.domain.staff.repository;

import com.wedding.management.domain.iam.model.Role;
import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.model.Staff;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class StaffSpecifications {
    public static Specification<Staff> filterStaffs(String name, Role role, StaffStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn lọc các bản ghi chưa bị xóa mềm
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("fullName")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}