package com.wedding.management.domain.auth.security;

import com.wedding.management.domain.auth.service.AuthTokenService;
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
import java.util.List;

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

                if (!Boolean.TRUE.equals(staff.getIsDeleted())
                        && staff.getStatus() == StaffStatus.ACTIVE
                        && staff.getAccountStatus() == StaffAccountStatus.ACTIVE) {
                    String roleName = staff.getRoleName() == null ? "STAFF" : staff.getRoleName().trim().toUpperCase();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    staff,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            });
        }

        filterChain.doFilter(request, response);
    }
}
