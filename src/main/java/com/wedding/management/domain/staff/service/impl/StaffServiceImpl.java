package com.wedding.management.domain.staff.service.impl;

import com.wedding.management.common.exception.BadRequestException;
import com.wedding.management.common.exception.ResourceNotFoundException;
import com.wedding.management.domain.iam.model.Role;
import com.wedding.management.domain.iam.repository.RoleRepository;
import com.wedding.management.domain.staff.dto.StaffRequest;
import com.wedding.management.domain.staff.dto.StaffResponse;
import com.wedding.management.domain.staff.enums.StaffStatus;
import com.wedding.management.domain.staff.model.Staff;
import com.wedding.management.domain.staff.repository.StaffRepository;
import com.wedding.management.domain.staff.repository.StaffSpecifications;
import com.wedding.management.domain.staff.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    // private final PasswordEncoder passwordEncoder; // Sẽ dùng khi config Spring Security hoàn tất

    @Override
    @Transactional
    public StaffResponse addStaff(StaffRequest request) {
        if (staffRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BadRequestException("Email này đã được sử dụng!");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Role tương ứng"));

        if ("DIRECTOR".equalsIgnoreCase(role.getName())) {
            throw new BadRequestException("Bạn không có quyền gán chức vụ Giám đốc!");
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        log.info(">>> MẬT KHẨU TẠM THỜI CHO {}: {}", request.getEmail(), tempPassword);

        Staff staff = Staff.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(tempPassword) // Tạm thời lưu plain text để test, sau này phải hash
                .role(role)
                .status(StaffStatus.ACTIVE) // Kích hoạt ngay sau khi gán role (Req 5)
                .build();

        log.info("Đã gửi email chào mừng tới {}", request.getEmail());
        return mapToResponse(staffRepository.save(staff));
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(UUID id, StaffRequest request) {
        Staff staff = staffRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên!"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Role tương ứng"));

        if ("DIRECTOR".equalsIgnoreCase(role.getName())) {
            throw new BadRequestException("Bạn không có quyền gán chức vụ Giám đốc!");
        }

        staff.setFullName(request.getFullName())
                .setRole(role);

        // Ghi log thay đổi (BaseEntity đã có Audit fields, có thể bổ sung bảng log riêng nếu cần)
        log.info("Nhân viên {} đã được cập nhật bởi admin", id);
        return mapToResponse(staffRepository.save(staff));
    }

    @Override
    public List<StaffResponse> searchStaffs(String name, UUID roleId, String status) {
        Role r = null;
        StaffStatus s = null;
        try {
            if (roleId != null) {
                r = roleRepository.findById(roleId).orElse(null);
            }
            if (status != null) s = StaffStatus.valueOf(status.toUpperCase());
        } catch (Exception ignored) {}

        Specification<Staff> spec = StaffSpecifications.filterStaffs(name, r, s);
        return staffRepository.findAll(spec).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteStaff(UUID id) {
        Staff staff = staffRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại!"));

        staff.setStatus(StaffStatus.INACTIVE);
        staff.setIsDeleted(true); // Soft delete (Req 4)

        // Logic Invalidate Session sẽ xử lý ở Security Context sau này
        log.warn("Nhân viên {} đã bị vô hiệu hóa và xóa mềm", staff.getEmail());
        staffRepository.save(staff);
    }

    private StaffResponse mapToResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .role(staff.getRole() != null ? staff.getRole().getName() : "N/A")
                .status(staff.getStatus().name())
                .build();
    }
}