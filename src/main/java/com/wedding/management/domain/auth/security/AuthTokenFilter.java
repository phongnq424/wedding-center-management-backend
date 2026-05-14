package com.wedding.management.domain.auth.security;

import com.wedding.management.domain.auth.service.AuthTokenService;
import com.wedding.management.domain.rbac.enums.PermissionStatus;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import com.wedding.management.domain.rbac.model.Permission;
import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.staff.enums.StaffAccountStatus;
import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.model.Staff;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;

    public AuthTokenFilter(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            authTokenService.findValidSessionByRawToken(token).ifPresent(session -> {
                Staff staff = session.getStaff();

                if (isAuthenticationAllowed(staff)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    staff,
                                    null,
                                    buildAuthorities(staff)
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            });
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticationAllowed(Staff staff) {
        return staff != null
                && !Boolean.TRUE.equals(staff.getIsDeleted())
                && staff.getStatus() == StaffStatus.ACTIVE
                && staff.getAccountStatus() == StaffAccountStatus.ACTIVE
                && staff.getRole() != null
                && !Boolean.TRUE.equals(staff.getRole().getIsDeleted())
                && staff.getRole().getStatus() == RoleStatus.ACTIVE;
    }

    private Set<SimpleGrantedAuthority> buildAuthorities(Staff staff) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        Role role = staff.getRole();

        if (role.getName() != null && !role.getName().isBlank()) {
            authorities.add(new SimpleGrantedAuthority(
                    "ROLE_" + role.getName().trim().toUpperCase()
            ));
        }

        if (role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                if (permission.getCode() != null
                        && !permission.getCode().isBlank()
                        && !Boolean.TRUE.equals(permission.getIsDeleted())
                        && permission.getStatus() == PermissionStatus.ACTIVE) {
                    authorities.add(new SimpleGrantedAuthority(
                            permission.getCode().trim().toUpperCase()
                    ));
                }
            }
        }

        return authorities;
    }
}