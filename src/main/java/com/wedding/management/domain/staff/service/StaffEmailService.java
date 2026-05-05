package com.wedding.management.domain.staff.service;

import java.util.UUID;

public interface StaffEmailService {
    void sendStaffActivationEmail(UUID staffId, String email, String fullName, String activationToken);
    void sendStaffRoleChangedEmail(UUID staffId, String email, String fullName, String roleName);
}