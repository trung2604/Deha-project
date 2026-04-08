package com.deha.HumanResourceManagement.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.deha.HumanResourceManagement.dto.user.UserRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.dto.user.UpdateUserRequest;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.repository.PayrollRepository;
import com.deha.HumanResourceManagement.repository.specification.UserSpecification;
import com.deha.HumanResourceManagement.service.IDepartmentService;
import com.deha.HumanResourceManagement.service.IOfficeService;
import com.deha.HumanResourceManagement.service.IUserService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.EmailVerificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService implements IUserService {
    private static final long MAX_AVATAR_SIZE_BYTES = 2 * 1024 * 1024;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final IDepartmentService departmentService;
    private final IOfficeService officeService;
    private final PositionRepository positionRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final OtRequestRepository otRequestRepository;
    private final OtSessionRepository otSessionRepository;
    private final PayrollRepository payrollRepository;
    private final AccessScopeService accessScopeService;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    private final EmailVerificationService emailVerificationService;


    public UserService(UserRepository userRepository, IDepartmentService departmentService, IOfficeService officeService, PositionRepository positionRepository, AttendanceLogRepository attendanceLogRepository, OtRequestRepository otRequestRepository, OtSessionRepository otSessionRepository, PayrollRepository payrollRepository, AccessScopeService accessScopeService, PasswordEncoder passwordEncoder, Cloudinary cloudinary, EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.departmentService = departmentService;
        this.officeService = officeService;
        this.positionRepository = positionRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.otRequestRepository = otRequestRepository;
        this.otSessionRepository = otSessionRepository;
        this.payrollRepository = payrollRepository;
        this.accessScopeService = accessScopeService;
        this.passwordEncoder = passwordEncoder;
        this.cloudinary = cloudinary;
        this.emailVerificationService = emailVerificationService;
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if(userRepository.existsByEmail(userRequest.getEmail())) {
            throw new ResourceAlreadyExistException("Email already exists");
        }
        UUID officeId = userRequest.getOffice() != null ? userRequest.getOffice().getId() : null;
        UUID departmentId = userRequest.getDepartment() != null ? userRequest.getDepartment().getId() : null;
        UUID positionId = userRequest.getPosition() != null ? userRequest.getPosition().getId() : null;
        Office office = officeService.findById(officeId);
        accessScopeService.assertCanManageOffice(office.getId());
        guardManagerCannotAssignAdmin(userRequest.getRole());

        Role targetRole = userRequest.getRole();
        boolean deptProvided = departmentId != null;
        boolean posProvided = positionId != null;

        Department department = null;
        Position position = null;

        if (targetRole == Role.MANAGER_OFFICE) {
            if (deptProvided || posProvided) {
                if (departmentId == null || positionId == null) {
                    throw new BadRequestException("Department and Position must be provided together or left empty for office manager");
                }
                department = departmentService.findDepartmentById(departmentId);
                position = positionRepository.findById(positionId).orElseThrow(
                        () -> new ResourceNotFoundException("Position not found with id: " + positionId));
            }
        } else if (targetRole == Role.MANAGER_DEPARTMENT) {
            if (!deptProvided) {
                throw new BadRequestException("Department is required for department manager");
            }
            department = departmentService.findDepartmentById(departmentId);
            if (posProvided) {
                position = positionRepository.findById(positionId).orElseThrow(
                        () -> new ResourceNotFoundException("Position not found with id: " + positionId));
            }
        } else {
            if (!deptProvided || !posProvided) {
                throw new BadRequestException("Department and Position are required for this role");
            }
            department = departmentService.findDepartmentById(departmentId);
            position = positionRepository.findById(positionId).orElseThrow(
                    () -> new ResourceNotFoundException("Position not found with id: " + positionId));
        }

        User user = new User();
        user.applyBasicInfo(
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getEmail(),
                userRequest.getRole()
        );
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.assignOfficeDepartmentAndPosition(office, department, position);
        user.markCreatedNow();
        user.setActive(false);
        userRepository.save(user);
        emailVerificationService.sendVerificationEmail(user);
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest UserRequest) {
        User current = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        assertExpectedVersion(UserRequest.getExpectedVersion(), user.getVersion(), "User");
        if (UserRequest.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }
        UUID officeId = UserRequest.getOffice() != null ? UserRequest.getOffice().getId() : null;
        UUID departmentId = UserRequest.getDepartment() != null ? UserRequest.getDepartment().getId() : null;
        UUID positionId = UserRequest.getPosition() != null ? UserRequest.getPosition().getId() : null;
        Office office = officeService.findById(officeId);
        accessScopeService.assertCanManageOffice(office.getId());
        guardManagerCannotAssignAdmin(UserRequest.getRole());

        Role targetRole = UserRequest.getRole();
        boolean deptProvided = departmentId != null;
        boolean posProvided = positionId != null;

        Department department = null;
        Position position = null;

        if (targetRole == Role.MANAGER_OFFICE) {
            if (deptProvided || posProvided) {
                if (departmentId == null || positionId == null) {
                    throw new BadRequestException("Department and Position must be provided together or left empty for office manager");
                }
                department = departmentService.findDepartmentById(departmentId);
                position = positionRepository.findById(positionId).orElseThrow(
                        () -> new ResourceNotFoundException("Position not found with id: " + positionId));
            }
        } else if (targetRole == Role.MANAGER_DEPARTMENT) {
            if (!deptProvided) {
                throw new BadRequestException("Department is required for department manager");
            }
            department = departmentService.findDepartmentById(departmentId);
            if (posProvided) {
                position = positionRepository.findById(positionId).orElseThrow(
                        () -> new ResourceNotFoundException("Position not found with id: " + positionId));
            }
        } else {
            if (!deptProvided || !posProvided) {
                throw new BadRequestException("Department and Position are required for this role");
            }
            department = departmentService.findDepartmentById(departmentId);
            position = positionRepository.findById(positionId).orElseThrow(
                    () -> new ResourceNotFoundException("Position not found with id: " + positionId));
        }

        User user = new User();
        user.setId(current.getId());
        user.setVersion(UserRequest.getExpectedVersion());
        user.setPassword(current.getPassword());
        user.setActive(current.isActive());
        user.setCreatedAt(current.getCreatedAt());
        user.setPhone(current.getPhone());
        user.setAvatarUrl(current.getAvatarUrl());

        user.applyBasicInfo(
                UserRequest.getFirstName(),
                UserRequest.getLastName(),
                UserRequest.getEmail(),
                UserRequest.getRole()
        );
        user.assignOfficeDepartmentAndPosition(office, department, position);
        userRepository.saveAndFlush(user);
        return UserResponse.fromEntity(user);
    }

//    public Page<UserResponse> getAllUsers(Pageable pageable) {
//        User actor = accessScopeService.currentUserOrThrow();
//        UUID scopedOfficeId = accessScopeService.isAdmin(actor) ? null : actor.getOffice().getId();
//        UUID scopedDepartmentId = accessScopeService.isDepartmentManager(actor)
//                ? (actor.getDepartment() != null ? actor.getDepartment().getId() : null)
//                : null;
//        Page<User> users = userRepository.searchUsers(null, scopedOfficeId, scopedDepartmentId, null, null, pageable);
//        return users.map(UserResponse::fromEntity);
//    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        accessScopeService.assertCanAccessUser(user);
        return UserResponse.fromEntity(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        accessScopeService.assertCanManageOffice(user.getOffice() != null ? user.getOffice().getId() : null);

        if (hasRelatedUserRecords(user.getId())) {
            throw new ConflictException("Cannot delete user because related records already exist (attendance/overtime/payroll). Deactivate the user instead.");
        }

        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Cannot delete user because related records already exist (attendance/overtime/payroll). Deactivate the user instead.");
        }
    }

    @Override
    public void deactivateUser(UUID id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        accessScopeService.assertCanManageOffice(target.getOffice() != null ? target.getOffice().getId() : null);

        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isManager(actor) && target.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Manager cannot deactivate admin user");
        }

        if (!target.isActive()) {
            return;
        }

        target.setActive(false);
        userRepository.saveAndFlush(target);
    }

    @Override
    public void resetUserPassword(UUID id, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("New password is required");
        }
        if (newPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }

        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        accessScopeService.assertCanManageOffice(target.getOffice() != null ? target.getOffice().getId() : null);

        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isManager(actor) && target.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Manager cannot reset admin password");
        }

        target.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(target);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Avatar file is required");
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new BadRequestException("Avatar file must be <= 2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed for avatar");
        }
        User actor = accessScopeService.currentUserOrThrow();
        UUID userId = actor.getId();
        if (actor.getAvatarUrl() != null && !actor.getAvatarUrl().isBlank()) {
            try {
                cloudinary.uploader().destroy(
                        "avatars/" + userId,
                        ObjectUtils.asMap(
                                "resource_type", "image",
                                "invalidate", true
                        )
                );
            } catch (Exception e) {
                log.warn("Failed to remove old avatar before upload for user {}", userId, e);
            }
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "avatars",
                            "public_id", userId.toString(),
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new RuntimeException("Avatar upload failed: secure_url is missing");
            }
            String avatarUrl = secureUrl.toString();
            actor.setAvatarUrl(avatarUrl);
            userRepository.saveAndFlush(actor);
            return avatarUrl;
        } catch (Exception e) {
            log.error("Avatar upload failed for user {}", userId, e);
            String reason = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "Unknown upload error";
            throw new BadRequestException("Avatar upload failed: " + reason);
        }
    }

    @Override
    public void removeAvatar() {
        User actor = accessScopeService.currentUserOrThrow();
        if (actor.getAvatarUrl() == null || actor.getAvatarUrl().isBlank()) {
            return;
        }

        UUID userId = actor.getId();
        try {
            cloudinary.uploader().destroy(
                    "avatars/" + userId,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "invalidate", true
                    )
            );
            actor.setAvatarUrl(null);
            userRepository.saveAndFlush(actor);
        } catch (Exception e) {
            throw new BadRequestException("Avatar remove failed. Please try again.");
        }
    }

//    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
//        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
//        User actor = accessScopeService.currentUserOrThrow();
//        UUID officeId = accessScopeService.isAdmin(actor) ? null : actor.getOffice().getId();
//        Page<User> users = userRepository.searchUsers(normalizedKeyword, officeId, null, null, null, pageable);
//        return users.map(UserResponse::fromEntity);
//    }

    @Override
    public Page<UserResponse> getUsersWithFilters(
            String keyword,
            UUID officeId,
            UUID departmentId,
            UUID positionId,
            Boolean active,
            Pageable pageable
    ) {
        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;

        User actor = accessScopeService.currentUserOrThrow();

        UUID scopedOfficeId;
        UUID scopedDepartmentId;

        if (accessScopeService.isAdmin(actor)) {
            scopedOfficeId = officeId;
            scopedDepartmentId = departmentId;

        } else if (accessScopeService.isOfficeManager(actor)) {
            scopedOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            scopedDepartmentId = departmentId;

            if (scopedOfficeId == null) {
                throw new ForbiddenException("Office manager must be assigned to an office");
            }

        } else if (accessScopeService.isDepartmentManager(actor)) {

            scopedOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            scopedDepartmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;

            if (scopedDepartmentId == null) {
                throw new ForbiddenException("Department manager must be assigned to a department");
            }

        } else {
            throw new ForbiddenException("You do not have permission to view users");
        }

        Specification<User> spec = Specification
                .where(UserSpecification.search(normalizedKeyword))
                .and(UserSpecification.hasOffice(scopedOfficeId))
                .and(UserSpecification.hasDepartment(scopedDepartmentId))
                .and(UserSpecification.hasPosition(positionId))
                .and(UserSpecification.isActive(active));

        Page<User> users = userRepository.findAll(spec, pageable);

        return users.map(UserResponse::fromEntity);
    }
    private void guardManagerCannotAssignAdmin(Role targetRole) {
        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isManager(actor) && targetRole == Role.ADMIN) {
            throw new ForbiddenException("Manager cannot assign admin role");
        }
    }

    private boolean hasRelatedUserRecords(UUID userId) {
        return attendanceLogRepository.existsByUser_Id(userId)
                || otRequestRepository.existsByUser_Id(userId)
                || otSessionRepository.existsByUser_Id(userId)
                || payrollRepository.existsByUser_Id(userId);
    }

//    private void assertExpectedVersion(Long expectedVersion, Long currentVersion, String resourceName) {
//        if (expectedVersion == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//        if (!Objects.equals(expectedVersion, currentVersion)) {
//            throw new ConflictException(resourceName + " was modified by another user. Please refresh and retry.");
//        }
//    }

//    private User mergeAndFlush(User user) {
//        if (entityManager != null) {
//            User merged = entityManager.merge(user);
//            entityManager.flush();
//            return merged;
//        }
//        return userRepository.saveAndFlush(user);
//    }
}



