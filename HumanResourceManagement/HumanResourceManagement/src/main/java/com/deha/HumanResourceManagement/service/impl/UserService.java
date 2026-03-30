package com.deha.HumanResourceManagement.service.impl;

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
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.specification.UserSpecification;
import com.deha.HumanResourceManagement.service.IDepartmentService;
import com.deha.HumanResourceManagement.service.IOfficeService;
import com.deha.HumanResourceManagement.service.IUserService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final IDepartmentService departmentService;
    private final IOfficeService officeService;
    private final PositionRepository positionRepository;
    private final AccessScopeService accessScopeService;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, IDepartmentService departmentService, IOfficeService officeService, PositionRepository positionRepository, AccessScopeService accessScopeService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentService = departmentService;
        this.officeService = officeService;
        this.positionRepository = positionRepository;
        this.accessScopeService = accessScopeService;
        this.passwordEncoder = passwordEncoder;
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

        if (targetRole == Role.ROLE_MANAGER_OFFICE) {
            // Office managers can be created/updated with only an office.
            // If department/position is provided, it must be provided together.
            if (deptProvided || posProvided) {
                if (departmentId == null || positionId == null) {
                    throw new BadRequestException("Department and Position must be provided together or left empty for office manager");
                }
                department = departmentService.findDepartmentById(departmentId);
                position = positionRepository.findById(positionId).orElseThrow(
                        () -> new ResourceNotFoundException("Position not found with id: " + positionId));
            }
        } else if (targetRole == Role.ROLE_MANAGER_DEPARTMENT) {
            // Department managers must belong to a department; position is optional.
            if (!deptProvided) {
                throw new BadRequestException("Department is required for department manager");
            }
            department = departmentService.findDepartmentById(departmentId);
            if (posProvided) {
                position = positionRepository.findById(positionId).orElseThrow(
                        () -> new ResourceNotFoundException("Position not found with id: " + positionId));
            }
        } else {
            // Non-manager roles require department + position assignment.
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
        user.activate();
        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest UserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

        if (targetRole == Role.ROLE_MANAGER_OFFICE) {
            // Office managers can be created/updated with only an office.
            // If department/position is provided, it must be provided together.
            if (deptProvided || posProvided) {
                if (departmentId == null || positionId == null) {
                    throw new BadRequestException("Department and Position must be provided together or left empty for office manager");
                }
                department = departmentService.findDepartmentById(departmentId);
                position = positionRepository.findById(positionId).orElseThrow(
                        () -> new ResourceNotFoundException("Position not found with id: " + positionId));
            }
        } else if (targetRole == Role.ROLE_MANAGER_DEPARTMENT) {
            // Department managers must belong to a department; position is optional.
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

        user.applyBasicInfo(
                UserRequest.getFirstName(),
                UserRequest.getLastName(),
                UserRequest.getEmail(),
                UserRequest.getRole()
        );
        user.assignOfficeDepartmentAndPosition(office, department, position);
        userRepository.save(user);
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
        userRepository.delete(user);
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

        } else if (accessScopeService.isDepartmentManager(actor)) {

            scopedOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            scopedDepartmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;

            if (scopedDepartmentId == null) {
                throw new ForbiddenException("Department manager must be assigned to a department");
            }

        } else {
            scopedOfficeId = actor.getOffice().getId();
            scopedDepartmentId = departmentId;
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
        if (accessScopeService.isManager(actor) && targetRole == Role.ROLE_ADMIN) {
            throw new ForbiddenException("Manager cannot assign admin role");
        }
    }
}



