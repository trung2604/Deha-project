package com.deha.HumanResourceManagement.service.ot.workflow;

import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OtRequestWorkflowServiceTest {

    private final OtRequestWorkflowService workflowService = new OtRequestWorkflowService();

    @Test
    void initialStatus_employee_shouldBePendingDepartment() {
        OtRequestStatus status = workflowService.initialStatus(Role.ROLE_EMPLOYEE);
        assertEquals(OtRequestStatus.PENDING_DEPARTMENT, status);
    }

    @Test
    void initialStatus_departmentManager_shouldBePendingOffice() {
        OtRequestStatus status = workflowService.initialStatus(Role.ROLE_MANAGER_DEPARTMENT);
        assertEquals(OtRequestStatus.PENDING_OFFICE, status);
    }

    @Test
    void initialStatus_officeManager_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> workflowService.initialStatus(Role.ROLE_MANAGER_OFFICE));
    }

    @Test
    void nextStatus_departmentManagerApprovePendingDepartment_shouldBeApproved() {
        OtRequestStatus next = workflowService.nextStatus(
                Role.ROLE_MANAGER_DEPARTMENT,
                OtRequestStatus.PENDING_DEPARTMENT,
                true
        );
        assertEquals(OtRequestStatus.APPROVED, next);
    }

    @Test
    void nextStatus_departmentManagerRejectPending_shouldBeRejected() {
        OtRequestStatus next = workflowService.nextStatus(
                Role.ROLE_MANAGER_DEPARTMENT,
                OtRequestStatus.PENDING,
                false
        );
        assertEquals(OtRequestStatus.REJECTED, next);
    }

    @Test
    void nextStatus_officeManagerApprovePendingOffice_shouldBeApproved() {
        OtRequestStatus next = workflowService.nextStatus(
                Role.ROLE_MANAGER_OFFICE,
                OtRequestStatus.PENDING_OFFICE,
                true
        );
        assertEquals(OtRequestStatus.APPROVED, next);
    }

    @Test
    void nextStatus_departmentManagerOnPendingOffice_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> workflowService.nextStatus(
                Role.ROLE_MANAGER_DEPARTMENT,
                OtRequestStatus.PENDING_OFFICE,
                true
        ));
    }

    @Test
    void nextStatus_onTerminalStatus_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> workflowService.nextStatus(
                Role.ROLE_MANAGER_DEPARTMENT,
                OtRequestStatus.APPROVED,
                true
        ));
    }

    @Test
    void nextStatus_employeeAsApprover_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> workflowService.nextStatus(
                Role.ROLE_EMPLOYEE,
                OtRequestStatus.PENDING_DEPARTMENT,
                true
        ));
    }

    @Test
    void pendingStatusSets_shouldMatchWorkflowQueues() {
        assertEquals(
                java.util.List.of(OtRequestStatus.PENDING_DEPARTMENT, OtRequestStatus.PENDING),
                workflowService.pendingStatusesForDepartmentManager()
        );
        assertEquals(
                java.util.List.of(OtRequestStatus.PENDING_OFFICE),
                workflowService.pendingStatusesForOfficeManager()
        );
    }
}


