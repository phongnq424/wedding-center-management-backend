package com.wedding.management.config.seed;

import com.wedding.management.domain.rbac.enums.PermissionStatus;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import com.wedding.management.domain.rbac.model.Permission;
import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.rbac.repository.PermissionRepository;
import com.wedding.management.domain.rbac.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Component
public class RbacPermissionSeeder implements CommandLineRunner {

    private static final String SYSTEM = "SYSTEM";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public RbacPermissionSeeder(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Permission> permissions = seedPermissions();

        seedRole("DIRECTOR", "System owner / highest-level administrator",
                allPermissionCodes(permissions));

        seedRole("OPERATIONS_MANAGER", "Operations manager",
                Set.of(
                        "DASHBOARD_VIEW",

                        "BOOKING_VIEW", "BOOKING_FULL_ACCESS",
                        "HALL_VIEW", "HALL_FULL_ACCESS",
                        "HALL_TYPE_VIEW", "HALL_TYPE_FULL_ACCESS",
                        "SHIFT_VIEW", "SHIFT_FULL_ACCESS",
                        "SERVICE_VIEW", "SERVICE_FULL_ACCESS",

                        "MENU_VIEW",
                        "PACKAGE_VIEW",

                        "STAFF_VIEW", "STAFF_FULL_ACCESS",
                        "ROLE_VIEW",
                        "PERMISSION_VIEW",

                        "REPORT_VIEW",
                        "AUDIT_VIEW"
                ));

        seedRole("EVENT_MANAGER", "Event manager",
                Set.of(
                        "DASHBOARD_VIEW",

                        "BOOKING_VIEW", "BOOKING_FULL_ACCESS",
                        "HALL_VIEW",
                        "SHIFT_VIEW",
                        "SERVICE_VIEW",
                        "MENU_VIEW",
                        "PACKAGE_VIEW"
                ));

        seedRole("MENU_MANAGER", "Menu manager",
                Set.of(
                        "DASHBOARD_VIEW",

                        "MENU_VIEW", "MENU_FULL_ACCESS",
                        "PACKAGE_VIEW", "PACKAGE_FULL_ACCESS",
                        "SERVICE_VIEW"
                ));

        seedRole("ACCOUNTANT", "Accountant",
                Set.of(
                        "DASHBOARD_VIEW",

                        "BOOKING_VIEW",
                        "PAYMENT_VIEW", "PAYMENT_FULL_ACCESS",
                        "INVOICE_VIEW", "INVOICE_FULL_ACCESS",
                        "REPORT_VIEW",

                        "AUTH_2FA_REQUIRED"
                ));

    }

    private Map<String, Permission> seedPermissions() {
        Map<String, Permission> result = new HashMap<>();

        seedModulePermissions(result, "DASHBOARD", "Dashboard");
        seedModulePermissions(result, "BOOKING", "Booking");
        seedModulePermissions(result, "HALL", "Hall");
        seedModulePermissions(result, "HALL_TYPE", "Hall Type");
        seedModulePermissions(result, "SHIFT", "Shift");
        seedModulePermissions(result, "SERVICE", "Service");
        seedModulePermissions(result, "MENU", "Menu");
        seedModulePermissions(result, "PACKAGE", "Wedding Package");
        seedModulePermissions(result, "STAFF", "Staff");
        seedModulePermissions(result, "ROLE", "Role");
        seedModulePermissions(result, "PERMISSION", "Permission");
        seedModulePermissions(result, "PAYMENT", "Payment");
        seedModulePermissions(result, "INVOICE", "Invoice");
        seedModulePermissions(result, "REPORT", "Report");
        seedModulePermissions(result, "AUDIT", "Audit");
        seedModulePermissions(result, "SETTING", "Setting");

        Permission twoFa = createOrUpdatePermission(
                "AUTH_2FA_REQUIRED",
                "Require two-factor authentication",
                "Require 2FA before issuing login session",
                "AUTH"
        );
        result.put(twoFa.getCode(), twoFa);

        return result;
    }

    private void seedModulePermissions(
            Map<String, Permission> result,
            String module,
            String displayName
    ) {
        Permission view = createOrUpdatePermission(
                module + "_VIEW",
                "View " + displayName,
                "Allow viewing/searching " + displayName + " data",
                module
        );

        Permission fullAccess = createOrUpdatePermission(
                module + "_FULL_ACCESS",
                "Full access to " + displayName,
                "Allow creating, updating, deleting, cancelling, or processing " + displayName + " data",
                module
        );

        result.put(view.getCode(), view);
        result.put(fullAccess.getCode(), fullAccess);
    }

    private Permission createOrUpdatePermission(
            String code,
            String name,
            String description,
            String module
    ) {
        String normalizedCode = code.trim().toUpperCase();
        String normalizedModule = module.trim().toUpperCase();

        Permission permission = permissionRepository.findByCode(normalizedCode)
                .orElseGet(() -> Permission.builder()
                        .code(normalizedCode)
                        .createdAt(Instant.now())
                        .createdBy(SYSTEM)
                        .isDeleted(false)
                        .build());

        permission.setName(name);
        permission.setDescription(description);
        permission.setModule(normalizedModule);
        permission.setStatus(PermissionStatus.ACTIVE);
        permission.setUpdatedAt(Instant.now());
        permission.setUpdatedBy(SYSTEM);

        return permissionRepository.save(permission);
    }

    private void seedRole(
            String roleName,
            String description,
            Set<String> permissionCodes
    ) {
        String normalizedRoleName = roleName.trim().toUpperCase();

        Role role = roleRepository.findByName(normalizedRoleName)
                .orElseGet(() -> Role.builder()
                        .name(normalizedRoleName)
                        .createdAt(Instant.now())
                        .createdBy(SYSTEM)
                        .isDeleted(false)
                        .build());

        Set<Permission> permissions = role.getPermissions() == null
                ? new HashSet<>()
                : new HashSet<>(role.getPermissions());

        for (String code : permissionCodes) {
            permissionRepository.findByCode(code.trim().toUpperCase())
                    .filter(permission -> !Boolean.TRUE.equals(permission.getIsDeleted()))
                    .filter(permission -> permission.getStatus() == PermissionStatus.ACTIVE)
                    .ifPresent(permissions::add);
        }

        role.setDescription(description);
        role.setStatus(RoleStatus.ACTIVE);
        role.setPermissions(permissions);
        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(SYSTEM);

        roleRepository.save(role);
    }

    private Set<String> allPermissionCodes(Map<String, Permission> permissions) {
        return new HashSet<>(permissions.keySet());
    }
}