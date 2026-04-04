package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtRequestResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.service.impl.OtRequestService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.ot.workflow.OtRequestWorkflowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class OtRequestServiceStageTransitionTest {

    @Test
    void decide_withoutExpectedVersion_shouldThrowBadRequest() {
        UUID requestId = UUID.randomUUID();

        Office office = new Office();
        office.setId(UUID.randomUUID());

        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOffice(office);

        User requestCreator = new User();
        requestCreator.setId(UUID.randomUUID());
        requestCreator.setRole(Role.ROLE_EMPLOYEE);
        requestCreator.setOffice(office);
        requestCreator.setDepartment(department);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.ROLE_MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtRequest otRequest = new OtRequest();
        otRequest.setId(requestId);
        otRequest.setVersion(2L);
        otRequest.setUser(requestCreator);
        otRequest.setOffice(office);
        otRequest.setLogDate(LocalDate.now());
        otRequest.setStatus(OtRequestStatus.PENDING_DEPARTMENT);

        OtRequestRepository otRequestRepository = mock(OtRequestRepository.class);
        when(otRequestRepository.findById(requestId)).thenReturn(Optional.of(otRequest));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtRequestService service = new OtRequestService(
                otRequestRepository,
                accessScopeService,
                new OtRequestWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);

        assertThrows(BadRequestException.class, () -> service.decide(requestId, decision));
        verify(otRequestRepository, never()).save(any(OtRequest.class));
    }

    @Test
    void decide_withStaleExpectedVersion_shouldThrowConflict() {
        UUID requestId = UUID.randomUUID();

        Office office = new Office();
        office.setId(UUID.randomUUID());

        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOffice(office);

        User requestCreator = new User();
        requestCreator.setId(UUID.randomUUID());
        requestCreator.setRole(Role.ROLE_EMPLOYEE);
        requestCreator.setOffice(office);
        requestCreator.setDepartment(department);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.ROLE_MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtRequest otRequest = new OtRequest();
        otRequest.setId(requestId);
        otRequest.setVersion(5L);
        otRequest.setUser(requestCreator);
        otRequest.setOffice(office);
        otRequest.setLogDate(LocalDate.now());
        otRequest.setStatus(OtRequestStatus.PENDING_DEPARTMENT);

        OtRequestRepository otRequestRepository = mock(OtRequestRepository.class);
        when(otRequestRepository.findById(requestId)).thenReturn(Optional.of(otRequest));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtRequestService service = new OtRequestService(
                otRequestRepository,
                accessScopeService,
                new OtRequestWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setExpectedVersion(4L);
        decision.setExpectedVersion(4L);

        assertThrows(ConflictException.class, () -> service.decide(requestId, decision));
        verify(otRequestRepository, never()).save(any(OtRequest.class));
    }

    @Test
    void departmentManager_approvesPendingDepartment_toPendingOffice() {
        UUID officeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department department = new Department();
        department.setId(departmentId);
        department.setOffice(office);

        User requestCreator = new User();
        requestCreator.setId(UUID.randomUUID());
        requestCreator.setRole(Role.ROLE_EMPLOYEE);
        requestCreator.setOffice(office);
        requestCreator.setDepartment(department);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.ROLE_MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtRequest otRequest = new OtRequest();
        otRequest.setId(requestId);
        otRequest.setVersion(1L);
        otRequest.setUser(requestCreator);
        otRequest.setOffice(office);
        otRequest.setLogDate(LocalDate.now());
        otRequest.setReason("OT");
        otRequest.setStatus(OtRequestStatus.PENDING_DEPARTMENT);

        OtRequestRepository otRequestRepository = mock(OtRequestRepository.class);
        when(otRequestRepository.findById(requestId)).thenReturn(Optional.of(otRequest));
        when(otRequestRepository.save(any(OtRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtRequestService service = new OtRequestService(
                otRequestRepository,
                accessScopeService,
                new OtRequestWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        OtRequestResponse response = service.decide(requestId, decision);

        assertEquals(OtRequestStatus.APPROVED, response.getStatus());
        assertNotNull(response.getApprovedById());
    }

    @Test
    void officeManager_approvesPendingOffice_toApproved() {
        UUID officeId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOffice(office);

        User requestCreator = new User();
        requestCreator.setId(UUID.randomUUID());
        requestCreator.setRole(Role.ROLE_EMPLOYEE);
        requestCreator.setOffice(office);
        requestCreator.setDepartment(department);

        User officeManager = new User();
        officeManager.setId(UUID.randomUUID());
        officeManager.setRole(Role.ROLE_MANAGER_OFFICE);
        officeManager.setOffice(office);

        OtRequest otRequest = new OtRequest();
        otRequest.setId(requestId);
        otRequest.setVersion(1L);
        otRequest.setUser(requestCreator);
        otRequest.setOffice(office);
        otRequest.setLogDate(LocalDate.now());
        otRequest.setReason("OT");
        otRequest.setStatus(OtRequestStatus.PENDING_OFFICE);

        OtRequestRepository otRequestRepository = mock(OtRequestRepository.class);
        when(otRequestRepository.findById(requestId)).thenReturn(Optional.of(otRequest));
        when(otRequestRepository.save(any(OtRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return officeManager;
            }
        };

        OtRequestService service = new OtRequestService(
                otRequestRepository,
                accessScopeService,
                new OtRequestWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        OtRequestResponse response = service.decide(requestId, decision);

        assertEquals(OtRequestStatus.APPROVED, response.getStatus());
        assertEquals(officeManager.getId(), response.getApprovedById());
    }

    @Test
    void departmentManager_scopeMismatch_shouldThrowForbidden() {
        UUID officeId = UUID.randomUUID();
        UUID correctDepartmentId = UUID.randomUUID();
        UUID wrongDepartmentId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department correctDept = new Department();
        correctDept.setId(correctDepartmentId);
        correctDept.setOffice(office);

        Department wrongDept = new Department();
        wrongDept.setId(wrongDepartmentId);
        wrongDept.setOffice(office);

        User requestCreator = new User();
        requestCreator.setId(UUID.randomUUID());
        requestCreator.setRole(Role.ROLE_EMPLOYEE);
        requestCreator.setOffice(office);
        requestCreator.setDepartment(correctDept);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.ROLE_MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(wrongDept);

        OtRequest otRequest = new OtRequest();
        otRequest.setId(requestId);
        otRequest.setVersion(1L);
        otRequest.setUser(requestCreator);
        otRequest.setOffice(office);
        otRequest.setLogDate(LocalDate.now());
        otRequest.setReason("OT");
        otRequest.setStatus(OtRequestStatus.PENDING_DEPARTMENT);

        OtRequestRepository otRequestRepository = mock(OtRequestRepository.class);
        when(otRequestRepository.findById(requestId)).thenReturn(Optional.of(otRequest));
        when(otRequestRepository.save(any(OtRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtRequestService service = new OtRequestService(
                otRequestRepository,
                accessScopeService,
                new OtRequestWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        Executable action = () -> service.decide(requestId, decision);
        assertThrows(ForbiddenException.class, action);
    }

    @Test
    void selfApproval_shouldThrowForbidden() {
        UUID officeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department department = new Department();
        department.setId(departmentId);
        department.setOffice(office);

        UUID managerId = UUID.randomUUID();

        User requestCreator = new User();
        requestCreator.setId(managerId);
        requestCreator.setRole(Role.ROLE_EMPLOYEE);
        requestCreator.setOffice(office);
        requestCreator.setDepartment(department);

        User departmentManager = new User();
        departmentManager.setId(managerId);
        departmentManager.setRole(Role.ROLE_MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtRequest otRequest = new OtRequest();
        otRequest.setId(requestId);
        otRequest.setVersion(1L);
        otRequest.setUser(requestCreator);
        otRequest.setOffice(office);
        otRequest.setLogDate(LocalDate.now());
        otRequest.setReason("OT");
        otRequest.setStatus(OtRequestStatus.PENDING_DEPARTMENT);

        OtRequestRepository otRequestRepository = mock(OtRequestRepository.class);
        when(otRequestRepository.findById(requestId)).thenReturn(Optional.of(otRequest));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtRequestService service = new OtRequestService(
                otRequestRepository,
                accessScopeService,
                new OtRequestWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        Executable action = () -> service.decide(requestId, decision);
        assertThrows(ForbiddenException.class, action);
    }
}

