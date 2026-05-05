package com.wedding.management.domain.auth.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.domain.auth.dto.*;
import com.wedding.management.domain.auth.service.AuthService;
import com.wedding.management.domain.auth.service.AuthTokenService;
import com.wedding.management.domain.auth.service.MfaService;
import com.wedding.management.domain.rbac.enums.RoleStatus;
import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.rbac.repository.RoleRepository;
import com.wedding.management.domain.staff.enums.StaffAccountStatus;
import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.model.Staff;
import com.wedding.management.domain.staff.repository.StaffRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_COMPLEXITY = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final MfaService mfaService;
    private final AuditLogRepository auditLogRepository;

    public AuthServiceImpl(
            StaffRepository staffRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService,
            MfaService mfaService,
            AuditLogRepository auditLogRepository
    ) {
        this.staffRepository = staffRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
        this.mfaService = mfaService;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        validateLoginFields(request.getEmail(), request.getPassword());

        Optional<Staff> optionalStaff = staffRepository.findByEmailAndIsDeletedFalse(request.getEmail());

        if (optionalStaff.isEmpty()) {
            throw new BadRequestException("MSG22: Sai email hoặc mật khẩu");
        }

        Staff staff = optionalStaff.get();

        if (staff.getAccountStatus() == StaffAccountStatus.LOCKED) {
            throw new BadRequestException("MSG3: Tài khoản đã bị khóa");
        }

        if (staff.getAccountStatus() == StaffAccountStatus.INACTIVE || staff.getStatus() == StaffStatus.INACTIVE) {
            throw new BadRequestException("MSG53: Tài khoản đã bị vô hiệu hóa");
        }

        if (staff.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), staff.getPasswordHash())) {
            incrementFailedAttempts(staff);
            throw new BadRequestException(staff.getFailedAttempts() >= MAX_FAILED_ATTEMPTS
                    ? "MSG3: Tài khoản đã bị khóa"
                    : "MSG22: Sai email hoặc mật khẩu");
        }

        staff.setFailedAttempts(0);
        staff.setLastLoginAt(Instant.now());
        staff.setUpdatedAt(Instant.now());
        staffRepository.save(staff);

        if (isFinancialRole(staff.getRoleName())) {
            String challengeId = mfaService.createChallenge(staff);
            return LoginResponse.builder()
                    .staffId(staff.getId())
                    .fullName(staff.getFullName())
                    .email(staff.getEmail())
                    .roleId(staff.getRoleId())
                    .roleName(staff.getRoleName())
                    .requires2FA(true)
                    .mfaChallengeId(challengeId)
                    .build();
        }

        return createLoginSessionResponse(staff);
    }

    @Override
    public LoginResponse verify2FA(Verify2FARequest request) {
        UUID staffId = mfaService.verify2FACode(request.getMfaChallengeId(), request.getInputCode());

        if (staffId == null) {
            throw new BadRequestException("MSG56: Mã xác thực không hợp lệ");
        }

        Staff staff = staffRepository.findByIdAndIsDeletedFalse(staffId)
                .orElseThrow(() -> new BadRequestException("MSG22: Tài khoản không tồn tại"));

        if (staff.getAccountStatus() != StaffAccountStatus.ACTIVE || staff.getStatus() != StaffStatus.ACTIVE) {
            throw new BadRequestException("MSG53: Tài khoản đã bị vô hiệu hóa");
        }

        return createLoginSessionResponse(staff);
    }

    @Override
    public void logout(String rawToken, String currentUserId) {
        authTokenService.revokeRawToken(rawToken, currentUserId, "LOGOUT");
    }

    @Override
    public void changePassword(ChangePasswordRequest request, Staff currentStaff) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()
                || request.getNewPassword() == null || request.getNewPassword().isBlank()
                || request.getConfirmNewPassword() == null || request.getConfirmNewPassword().isBlank()) {
            throw new BadRequestException("MSG2: Vui lòng nhập đầy đủ thông tin");
        }

        if (currentStaff.getPasswordHash() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), currentStaff.getPasswordHash())) {
            throw new BadRequestException("MSG22: Mật khẩu hiện tại không đúng");
        }

        if (!PASSWORD_COMPLEXITY.matcher(request.getNewPassword()).matches()) {
            throw new BadRequestException("MSG25: Mật khẩu mới không đạt yêu cầu bảo mật");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("MSG4: Mật khẩu xác nhận không khớp");
        }

        if (passwordEncoder.matches(request.getNewPassword(), currentStaff.getPasswordHash())) {
            throw new BadRequestException("MSG34: Mật khẩu mới không được trùng mật khẩu hiện tại");
        }

        currentStaff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        currentStaff.setUpdatedAt(Instant.now());
        currentStaff.setUpdatedBy(currentStaff.getId().toString());
        staffRepository.save(currentStaff);

        authTokenService.revokeAllSessions(
                currentStaff.getId(),
                currentStaff.getId().toString(),
                "PASSWORD_CHANGED"
        );

        saveAuditLog(currentStaff.getId().toString(), "CHANGE_PASSWORD", currentStaff.getId(), currentStaff.getFullName());
    }

    @Override
    public LoginResponse seedFirstAdmin(SeedAdminRequest request) {
        if (!staffRepository.findAllActive().isEmpty()) {
            throw new BadRequestException("Admin seed is disabled because staff already exists");
        }

        validateLoginFields(request.getEmail(), request.getPassword());

        if (!PASSWORD_COMPLEXITY.matcher(request.getPassword()).matches()) {
            throw new BadRequestException("MSG25: Mật khẩu không đạt yêu cầu bảo mật");
        }

        Role role = roleRepository.findByName("OPERATIONS_MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("OPERATIONS_MANAGER")
                        .description("Initial administrator role")
                        .status(RoleStatus.ACTIVE)
                        .createdBy("SYSTEM")
                        .createdAt(Instant.now())
                        .isDeleted(false)
                        .build()));

        Staff admin = Staff.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .description("Initial admin account")
                .staffImage(request.getStaffImage() == null || request.getStaffImage().isBlank()
                        ? "https://example.com/default-admin.png"
                        : request.getStaffImage())
                .roleId(role.getId())
                .roleName(role.getName())
                .status(StaffStatus.ACTIVE)
                .accountStatus(StaffAccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .failedAttempts(0)
                .createdBy("SYSTEM")
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Staff savedAdmin = staffRepository.save(admin);
        saveAuditLog(savedAdmin.getId().toString(), "SEED_FIRST_ADMIN", savedAdmin.getId(), savedAdmin.getFullName());

        return createLoginSessionResponse(savedAdmin);
    }

    private void validateLoginFields(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException("MSG2: Email và mật khẩu không được để trống");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("MSG31: Email không hợp lệ");
        }
    }

    private void incrementFailedAttempts(Staff staff) {
        int failedAttempts = staff.getFailedAttempts() == null ? 0 : staff.getFailedAttempts();
        failedAttempts++;
        staff.setFailedAttempts(failedAttempts);
        staff.setUpdatedAt(Instant.now());

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            staff.setAccountStatus(StaffAccountStatus.LOCKED);
        }

        staffRepository.save(staff);
    }

    private LoginResponse createLoginSessionResponse(Staff staff) {
        AuthTokenService.TokenPair tokenPair = authTokenService.createSession(staff, staff.getId().toString());

        saveAuditLog(staff.getId().toString(), "LOGIN", staff.getId(), staff.getFullName());

        return LoginResponse.builder()
                .staffId(staff.getId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .roleId(staff.getRoleId())
                .roleName(staff.getRoleName())
                .requires2FA(false)
                .accessToken(tokenPair.token())
                .expiresAt(tokenPair.expiresAt())
                .build();
    }

    private boolean isFinancialRole(String roleName) {
        if (roleName == null) {
            return false;
        }

        String normalized = roleName.trim().toUpperCase();
        return normalized.equals("ACCOUNTANT")
                || normalized.equals("FINANCE_MANAGER")
                || normalized.equals("FINANCIAL_ROLE");
    }

    private void saveAuditLog(String userId, String action, UUID targetId, String targetName) {
        try {
            UUID userUUID = UUID.fromString(userId);
            AuditLog auditLog = AuditLog.builder()
                    .userId(userUUID)
                    .action(action)
                    .targetId(targetId)
                    .targetName(targetName)
                    .createdAt(Instant.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
