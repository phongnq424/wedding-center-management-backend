package com.wedding.management.domain.staff.service.impl;

import com.wedding.management.common.audit.AuditLog;
import com.wedding.management.common.audit.AuditLogRepository;
import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.rbac.model.Role;
import com.wedding.management.domain.staff.dto.RoleOptionDTO;
import com.wedding.management.domain.staff.dto.StaffRequest;
import com.wedding.management.domain.staff.dto.StaffResponse;
import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.model.Staff;
import com.wedding.management.domain.staff.repository.StaffRepository;
import com.wedding.management.domain.staff.service.CurrentUserVerifier;
import com.wedding.management.domain.staff.service.RoleLookupService;
import com.wedding.management.domain.staff.service.StaffEmailService;
import com.wedding.management.domain.staff.service.StaffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final AuditLogRepository auditLogRepository;
    private final CurrentUserVerifier currentUserVerifier;
    private final StaffEmailService staffEmailService;
    private final RoleLookupService roleLookupService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(0|\\+84)[0-9]{9,10}$");

    public StaffServiceImpl(
            StaffRepository staffRepository,
            AuditLogRepository auditLogRepository,
            CurrentUserVerifier currentUserVerifier,
            StaffEmailService staffEmailService,
            RoleLookupService roleLookupService
    ) {
        this.staffRepository = staffRepository;
        this.auditLogRepository = auditLogRepository;
        this.currentUserVerifier = currentUserVerifier;
        this.staffEmailService = staffEmailService;
        this.roleLookupService = roleLookupService;
    }

    @Override
    public StaffResponse createStaff(StaffRequest request, String currentUserId) {
        // BR-CST-2: ValidateStaffInput(fullName, email, phoneNumber, role, staffImage)
        validateStaffInput(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getRoleId(),
                request.getStaffImage()
        );

        // BR-CST-3: CheckEmailUnique(email)
        if (staffRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BadRequestException("MSG27: Email đã tồn tại");
        }

        // BR-CST-3: CheckPhoneNumberUnique(phoneNumber)
        if (staffRepository.existsByPhoneNumberAndIsDeletedFalse(request.getPhoneNumber())) {
            throw new BadRequestException("MSG26: Số điện thoại đã tồn tại");
        }

        String activationToken = UUID.randomUUID().toString();

        Role role = roleLookupService.getAvailableRoleForStaff(request.getRoleId());

        Staff staff = Staff.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .description(request.getDescription())
                .staffImage(request.getStaffImage())
                .role(role)
                .status(StaffStatus.INACTIVE)
                .activationToken(activationToken)
                .activationTokenCreatedAt(Instant.now())
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .isDeleted(false)
                .build();

        Staff savedStaff = staffRepository.save(staff);

        // BR-CST-5: SaveAuditLog(...)
        saveAuditLog(currentUserId, "ADD_STAFF", savedStaff.getId(), savedStaff.getFullName());

        // BR-CST-7: SendStaffActivationEmail(...)
        staffEmailService.sendStaffActivationEmail(
                savedStaff.getId(),
                savedStaff.getEmail(),
                savedStaff.getFullName(),
                activationToken
        );

        return mapToStaffResponse(savedStaff);
    }

    @Override
    public StaffResponse updateStaff(UUID staffId, StaffRequest request, String currentUserId, long lastModifiedAt) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        if (Boolean.TRUE.equals(staff.getIsDeleted())) {
            throw new ResourceNotFoundException("Nhân viên đã bị xóa");
        }

        // BR-UST-2: ValidateStaffInput(...)
        validateStaffInput(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getRoleId(),
                request.getStaffImage()
        );

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new BadRequestException("MSG2: Mật khẩu hiện tại không được để trống");
        }

        // BR-UST-3: VerifyCurrentPassword(currentUserId, currentPassword)
        if (!currentUserVerifier.verifyCurrentPassword(currentUserId, request.getCurrentPassword())) {
            throw new BadRequestException("MSG22: Mật khẩu hiện tại không đúng");
        }

        // BR-UST-4: CheckEmailUnique(email, staffId)
        if (!staff.getEmail().equalsIgnoreCase(request.getEmail())
                && staffRepository.existsByEmailExcludingId(request.getEmail(), staffId)) {
            throw new BadRequestException("MSG27: Email đã tồn tại");
        }

        // BR-UST-4: CheckPhoneNumberUnique(phoneNumber, staffId)
        if (!staff.getPhoneNumber().equals(request.getPhoneNumber())
                && staffRepository.existsByPhoneNumberExcludingId(request.getPhoneNumber(), staffId)) {
            throw new BadRequestException("MSG26: Số điện thoại đã tồn tại");
        }

        Role newRole = roleLookupService.getAvailableRoleForStaff(request.getRoleId());

        boolean roleChanged = staff.getRole() == null
                || !staff.getRole().getId().equals(newRole.getId());

        // BR-UST-4: CheckVersionConflict(staffId, userLastModifiedAt)
        if (staff.getUpdatedAt() != null && staff.getUpdatedAt().toEpochMilli() != lastModifiedAt) {
            throw new BadRequestException("MSG62: Dữ liệu đã được sửa đổi bởi người khác. Vui lòng tải lại trang.");
        }

        // BR-UST-5: UpdateStaff(...)
        staff.setFullName(request.getFullName());
        staff.setEmail(request.getEmail());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setDescription(request.getDescription());
        staff.setStaffImage(request.getStaffImage());
        staff.setRole(newRole);
        staff.setStatus(request.getStatus() == null ? staff.getStatus() : request.getStatus());
        staff.setUpdatedBy(currentUserId);
        staff.setUpdatedAt(Instant.now());

        Staff updatedStaff = staffRepository.save(staff);

        // BR-UST-6: SaveAuditLog(...)
        saveAuditLog(currentUserId, "UPDATE_STAFF", updatedStaff.getId(), updatedStaff.getFullName());

        // BR-UST-5: Staff member is notified of role change via email
        if (roleChanged) {
            staffEmailService.sendStaffRoleChangedEmail(
                    updatedStaff.getId(),
                    updatedStaff.getEmail(),
                    updatedStaff.getFullName(),
                    updatedStaff.getRole().getName()
            );
        }

        return mapToStaffResponse(updatedStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> searchStaff(
            String fullName,
            String email,
            String phoneNumber,
            UUID roleId,
            StaffStatus status
    ) {
        // BR-SST-1 / BR-SST-2: RetrieveAllStaff + SearchStaff(...)
        List<Staff> staffList = staffRepository.findAllActive();

        if (fullName != null && !fullName.isBlank()) {
            String keyword = fullName.toLowerCase();
            staffList = staffList.stream()
                    .filter(s -> s.getFullName() != null && s.getFullName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        if (email != null && !email.isBlank()) {
            String keyword = email.toLowerCase();
            staffList = staffList.stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            staffList = staffList.stream()
                    .filter(s -> s.getPhoneNumber() != null && s.getPhoneNumber().contains(phoneNumber))
                    .collect(Collectors.toList());
        }

        if (roleId != null) {
            staffList = staffList.stream()
                    .filter(s -> s.getRole() != null && s.getRole().getId().equals(roleId))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            staffList = staffList.stream()
                    .filter(s -> s.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return staffList.stream()
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStaff(UUID staffId, String currentPassword, String currentUserId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        if (Boolean.TRUE.equals(staff.getIsDeleted())) {
            throw new ResourceNotFoundException("Nhân viên đã bị xóa");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BadRequestException("MSG2: Mật khẩu hiện tại không được để trống");
        }

        // BR-DST-3: VerifyCurrentPassword(currentUserId, currentPassword)
        if (!currentUserVerifier.verifyCurrentPassword(currentUserId, currentPassword)) {
            throw new BadRequestException("MSG22: Mật khẩu hiện tại không đúng");
        }

        // BR-DST-4: SoftDeleteStaff(staffId, deletedBy, deletedAt)
        staff.setIsDeleted(true);
        staff.setStatus(StaffStatus.INACTIVE);
        staff.setDeletedBy(currentUserId);
        staff.setDeletedAt(Instant.now());
        staff.setUpdatedBy(currentUserId);
        staff.setUpdatedAt(Instant.now());

        staffRepository.save(staff);

        // BR-DST-5: SaveAuditLog(...)
        saveAuditLog(currentUserId, "DELETE_STAFF", staffId, staff.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAllActive().stream()
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(UUID staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        if (Boolean.TRUE.equals(staff.getIsDeleted())) {
            throw new ResourceNotFoundException("Nhân viên đã bị xóa");
        }

        return mapToStaffResponse(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleOptionDTO> getAvailableRoles() {
        return roleLookupService.getAvailableRolesExceptDirector();
    }

    private void validateStaffInput(
            String fullName,
            String email,
            String phoneNumber,
            UUID roleId,
            String staffImage
    ) {
        // MSG2: all fields except description must be filled
        if (fullName == null || fullName.isBlank()) {
            throw new BadRequestException("MSG2: Họ tên nhân viên không được để trống");
        }

        if (email == null || email.isBlank()) {
            throw new BadRequestException("MSG2: Email không được để trống");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BadRequestException("MSG2: Số điện thoại không được để trống");
        }

        if (staffImage == null || staffImage.isBlank()) {
            throw new BadRequestException("MSG2: Ảnh nhân viên không được để trống");
        }

        // MSG31: invalid email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("MSG31: Email không hợp lệ");
        }

        // MSG30: invalid phone number
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new BadRequestException("MSG30: Số điện thoại không hợp lệ");
        }
    }

    private StaffResponse mapToStaffResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .description(staff.getDescription())
                .staffImage(staff.getStaffImage())
                .roleId(staff.getRole() == null ? null : staff.getRole().getId())
                .roleName(staff.getRole() == null ? null : staff.getRole().getName())
                .status(staff.getStatus())
                .lastModifiedAt(staff.getUpdatedAt())
                .lastModifiedBy(staff.getUpdatedBy())
                .build();
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
        } catch (IllegalArgumentException e) {
            // If userId is not a valid UUID, skip audit logging.
            // This follows the current project style in Hall/HallType service.
        }
    }
}