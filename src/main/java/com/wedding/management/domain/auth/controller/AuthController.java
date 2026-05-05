package com.wedding.management.domain.auth.controller;

import com.wedding.management.common.dto.ApiResponse;
import com.wedding.management.domain.auth.dto.*;
import com.wedding.management.domain.auth.service.AuthService;
import com.wedding.management.domain.staff.model.Staff;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message(response.isRequires2FA()
                        ? "Display 2FA Prompt"
                        : "MSG23: Đăng nhập thành công")
                .data(response)
                .build());
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verify2FA(@Valid @RequestBody Verify2FARequest request) {
        LoginResponse response = authService.verify2FA(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("MSG23: Đăng nhập thành công")
                .data(response)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            Authentication authentication
    ) {
        String token = extractBearerToken(authorizationHeader);
        String currentUserId = authentication == null ? "SYSTEM" : authentication.getName();

        authService.logout(token, currentUserId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG58: Đăng xuất thành công")
                .build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        Staff currentStaff = getCurrentStaff(authentication);
        authService.changePassword(request, currentStaff);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("MSG52: Đổi mật khẩu thành công. Vui lòng đăng nhập lại")
                .build());
    }

    @PostMapping("/seed-first-admin")
    public ResponseEntity<ApiResponse<LoginResponse>> seedFirstAdmin(@Valid @RequestBody SeedAdminRequest request) {
        LoginResponse response = authService.seedFirstAdmin(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Initial admin account created")
                .data(response)
                .build());
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    private Staff getCurrentStaff(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Staff staff)) {
            throw new IllegalStateException("Authenticated staff is required");
        }
        return staff;
    }
}
