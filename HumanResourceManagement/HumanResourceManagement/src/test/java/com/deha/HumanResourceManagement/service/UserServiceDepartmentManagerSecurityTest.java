package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.user.UserRequest;
import com.deha.HumanResourceManagement.dto.user.UpdateUserRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.repository.PayrollRepository;
import com.deha.HumanResourceManagement.service.impl.UserService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.EmailVerificationService;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceDepartmentManagerSecurityTest {

    @Test
    void updateUser_withoutExpectedVersion_shouldThrowBadRequest() {
        User actor = new User();
        actor.setId(UUID.randomUUID());
        actor.setRole(Role.ADMIN);

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return actor;
            }
        };

        User existing = new User();
        existing.setId(UUID.randomUUID());
        existing.setVersion(3L);

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        UserService userService = new UserService(
                userRepository,
                mock(IDepartmentService.class),
                mock(IOfficeService.class),
                mock(PositionRepository.class),
                mock(AttendanceLogRepository.class),
                mock(OtRequestRepository.class),
                mock(OtSessionRepository.class),
                mock(PayrollRepository.class),
                accessScopeService,
                mock(PasswordEncoder.class),
                mock(Cloudinary.class),
                mock(EmailVerificationService.class)
        );

        UpdateUserRequest payload = new UpdateUserRequest();
        payload.setFirstName("A");
        payload.setLastName("B");
        payload.setEmail("a@b.com");
        payload.setRole(Role.EMPLOYEE);

        assertThrows(BadRequestException.class, () -> userService.updateUser(existing.getId(), payload));
    }

    @Test
    void updateUser_withStaleExpectedVersion_shouldStillUpdateInDetachedFlow() {
        User actor = new User();
        actor.setId(UUID.randomUUID());
        actor.setRole(Role.ADMIN);

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return actor;
            }
        };

        User existing = new User();
        existing.setId(UUID.randomUUID());
        existing.setVersion(3L);

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById(existing.getId())).thenReturn(Optional.of(existing));

        IOfficeService officeService = mock(IOfficeService.class);
        IDepartmentService departmentService = mock(IDepartmentService.class);
        PositionRepository positionRepository = mock(PositionRepository.class);

        Office office = new Office();
        office.setId(UUID.randomUUID());
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOffice(office);
        com.deha.HumanResourceManagement.entity.Position position = new com.deha.HumanResourceManagement.entity.Position();
        position.setId(UUID.randomUUID());
        position.setDepartment(department);

        when(officeService.findById(office.getId())).thenReturn(office);
        when(departmentService.findDepartmentById(department.getId())).thenReturn(department);
        when(positionRepository.findById(position.getId())).thenReturn(Optional.of(position));

        UserService userService = new UserService(
                userRepository,
                departmentService,
                officeService,
                positionRepository,
                mock(AttendanceLogRepository.class),
                mock(OtRequestRepository.class),
                mock(OtSessionRepository.class),
                mock(PayrollRepository.class),
                accessScopeService,
                mock(PasswordEncoder.class),
                mock(Cloudinary.class),
                mock(EmailVerificationService.class)
        );

        UpdateUserRequest payload = new UpdateUserRequest();
        payload.setFirstName("A");
        payload.setLastName("B");
        payload.setEmail("a@b.com");
        payload.setRole(Role.EMPLOYEE);
        payload.setOffice(office);
        payload.setDepartment(department);
        payload.setPosition(position);
        payload.setExpectedVersion(2L);

        assertDoesNotThrow(() -> userService.updateUser(existing.getId(), payload));
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void departmentManager_shouldBeForbiddenToCreateUser() {
        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.MANAGER_DEPARTMENT);

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        IOfficeService officeService = mock(IOfficeService.class);
        Office office = new Office();
        office.setId(UUID.randomUUID());
        when(officeService.findById(any(UUID.class))).thenReturn(office);

        IDepartmentService departmentService = mock(IDepartmentService.class);
        PositionRepository positionRepository = mock(PositionRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UserService userService = new UserService(
                userRepository,
                departmentService,
                officeService,
                positionRepository,
                mock(AttendanceLogRepository.class),
                mock(OtRequestRepository.class),
                mock(OtSessionRepository.class),
                mock(PayrollRepository.class),
                accessScopeService,
                passwordEncoder,
                mock(Cloudinary.class),
                mock(EmailVerificationService.class)
        );

        UserRequest payload = new UserRequest();
        payload.setFirstName("A");
        payload.setLastName("B");
        payload.setEmail("a@b.com");
        payload.setPassword("12345678");
        payload.setOffice(office);
        payload.setRole(Role.EMPLOYEE);

        assertThrows(ForbiddenException.class, () -> userService.createUser(payload));
    }

    @Test
    void departmentManager_getUsersWithFilters_shouldScopeToTheirDepartment() {
        UUID officeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department department = new Department();
        department.setId(departmentId);
        department.setOffice(office);

        User actor = new User();
        actor.setId(UUID.randomUUID());
        actor.setRole(Role.MANAGER_DEPARTMENT);
        actor.setOffice(office);
        actor.setDepartment(department);

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return actor;
            }
        };

        UserRepository userRepository = mock(UserRepository.class);

        User employeeUser = new User();
        employeeUser.setId(UUID.randomUUID());
        employeeUser.setFirstName("John");
        employeeUser.setLastName("Doe");
        employeeUser.setEmail("john@doe.com");
        employeeUser.setRole(Role.EMPLOYEE);
        employeeUser.setOffice(office);
        employeeUser.setDepartment(department);
        employeeUser.setActive(true);

        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(employeeUser), pageable, 1));

        IDepartmentService departmentService = mock(IDepartmentService.class);
        IOfficeService officeService = mock(IOfficeService.class);
        PositionRepository positionRepository = mock(PositionRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UserService userService = new UserService(
                userRepository,
                departmentService,
                officeService,
                positionRepository,
                mock(AttendanceLogRepository.class),
                mock(OtRequestRepository.class),
                mock(OtSessionRepository.class),
                mock(PayrollRepository.class),
                accessScopeService,
                passwordEncoder,
                mock(Cloudinary.class),
                mock(EmailVerificationService.class)
        );

        // Pass a different office/department in request payload, service must ignore them for department managers.
        Page<UserResponse> result = userService.getUsersWithFilters(
                "  kw  ",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }
}

