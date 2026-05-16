package com.wedding.management.domain.payment.service.impl;

import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.domain.auth.service.MfaService;
import com.wedding.management.domain.payment.dto.PaymentOtpChallengeResponse;
import com.wedding.management.domain.payment.service.TwoFactorVerificationService;
import com.wedding.management.domain.staff.enums.StaffAccountStatus;
import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.model.Staff;
import com.wedding.management.domain.staff.repository.StaffRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class DefaultTwoFactorVerificationService implements TwoFactorVerificationService {

    private static final long MFA_CODE_DURATION_SECONDS = 5 * 60;

    private final MfaService mfaService;
    private final StaffRepository staffRepository;

    public DefaultTwoFactorVerificationService(
            MfaService mfaService,
            StaffRepository staffRepository
    ) {
        this.mfaService = mfaService;
        this.staffRepository = staffRepository;
    }

    @Override
    public PaymentOtpChallengeResponse createPaymentProcessingChallenge(
            UUID paymentId,
            String currentUserId
    ) {
        Staff staff = getCurrentAuthenticatedStaff();

        String challengeId = mfaService.createChallenge(staff);

        return PaymentOtpChallengeResponse.builder()
                .paymentId(paymentId)
                .mfaChallengeId(challengeId)
                .email(maskEmail(staff.getEmail()))
                .expiresInSeconds(MFA_CODE_DURATION_SECONDS)
                .build();
    }

    @Override
    public boolean verify(
            String currentUserId,
            String challengeId,
            String inputCode
    ) {
        if (!StringUtils.hasText(challengeId)
                || !StringUtils.hasText(inputCode)) {
            return false;
        }

        Staff currentStaff;

        try {
            currentStaff = getCurrentAuthenticatedStaff();
        } catch (BadRequestException ex) {
            return false;
        }

        UUID verifiedStaffId = mfaService.verify2FACode(
                challengeId,
                inputCode.trim()
        );

        return verifiedStaffId != null
                && verifiedStaffId.equals(currentStaff.getId());
    }

    private Staff getCurrentAuthenticatedStaff() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("MSG56: Không xác định được nhân viên hiện tại");
        }

        Object principal = authentication.getPrincipal();

        Staff staff;

        if (principal instanceof Staff staffPrincipal) {
            staff = staffPrincipal;
        } else if (principal instanceof String principalName) {
            staff = findStaffByPrincipalName(principalName);
        } else {
            throw new BadRequestException("MSG56: Nhân viên hiện tại không hợp lệ");
        }

        validateActiveStaff(staff);

        return staff;
    }

    private Staff findStaffByPrincipalName(String principalName) {
        if (!StringUtils.hasText(principalName)) {
            throw new BadRequestException("MSG56: Không xác định được nhân viên hiện tại");
        }

        String value = principalName.trim();

        try {
            UUID staffId = UUID.fromString(value);

            return staffRepository.findByIdAndIsDeletedFalse(staffId)
                    .orElseThrow(() -> new BadRequestException("MSG22: Tài khoản không tồn tại"));
        } catch (IllegalArgumentException ignored) {
            return staffRepository.findByEmailAndIsDeletedFalse(value)
                    .orElseThrow(() -> new BadRequestException("MSG22: Tài khoản không tồn tại"));
        }
    }

    private void validateActiveStaff(Staff staff) {
        if (staff == null || staff.getId() == null) {
            throw new BadRequestException("MSG56: Nhân viên hiện tại không hợp lệ");
        }

        if (staff.getAccountStatus() != StaffAccountStatus.ACTIVE
                || staff.getStatus() != StaffStatus.ACTIVE) {
            throw new BadRequestException("MSG53: Tài khoản đã bị vô hiệu hóa");
        }

        if (Boolean.TRUE.equals(staff.getIsDeleted())) {
            throw new BadRequestException("MSG53: Tài khoản đã bị vô hiệu hóa");
        }
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }

        return name.charAt(0)
                + "***"
                + name.charAt(name.length() - 1)
                + "@"
                + domain;
    }
}