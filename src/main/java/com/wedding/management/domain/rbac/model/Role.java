package com.wedding.management.domain.rbac.model;

import com.wedding.management.common.entity.BaseEntity;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Accessors(chain = true)
public class Role extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name; // Ví dụ: "OPERATIONS_MANAGER"

    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoleStatus status = RoleStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}