package com.wedding.management.domain.staff.service.impl;

import com.wedding.management.domain.staff.service.StaffEmailService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StaffEmailServiceImpl implements StaffEmailService {

    @Override
    public void sendStaffActivationEmail(UUID staffId, String email, String fullName, String activationToken) {
        String activationLink = "http://localhost:8080/api/v1/staff/activate?token=" + activationToken;

        System.out.println("=== SendStaffActivationEmail ===");
        System.out.println("To: " + email);
        System.out.println("Subject: Activate Your Staff Account");
        System.out.println("Staff Name: " + fullName);
        System.out.println("Activation Link: " + activationLink);
    }

    @Override
    public void sendStaffRoleChangedEmail(UUID staffId, String email, String fullName, String roleName) {
        System.out.println("=== SendStaffRoleChangedEmail ===");
        System.out.println("To: " + email);
        System.out.println("Subject: Staff Role Updated");
        System.out.println("Staff Name: " + fullName);
        System.out.println("New Role: " + roleName);
    }
}