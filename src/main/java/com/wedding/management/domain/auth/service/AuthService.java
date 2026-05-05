package com.wedding.management.domain.auth.service;

import com.wedding.management.domain.auth.dto.*;
import com.wedding.management.domain.staff.model.Staff;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse verify2FA(Verify2FARequest request);
    void logout(String rawToken, String currentUserId);
    void changePassword(ChangePasswordRequest request, Staff currentStaff);
    LoginResponse seedFirstAdmin(SeedAdminRequest request);
}
