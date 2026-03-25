package com.wedding.management.domain.iam.model;

import com.wedding.management.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Permission extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String code; // Ví dụ: "HALL_VIEW", "STAFF_MANAGE"

    @Column(nullable = false)
    private String name; // Tên hiển thị: "Xem danh sách sảnh"

    private String module; // Nhóm: "HALL", "STAFF", "SERVICE"
}