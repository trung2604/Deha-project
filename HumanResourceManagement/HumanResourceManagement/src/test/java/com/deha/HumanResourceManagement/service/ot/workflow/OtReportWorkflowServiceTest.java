package com.deha.HumanResourceManagement.service.ot.workflow;

import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OtReportWorkflowServiceTest {

    private final OtReportWorkflowService workflowService = new OtReportWorkflowService();

    @Test
    void initialStatus_employee_shouldBePendingDepartment() {
        OtReportStatus status = workflowService.initialStatus(Role.ROLE_EMPLOYEE);
        assertEquals(OtReportStatus.PENDING_DEPARTMENT, status);
    }

    @Test
    void initialStatus_departmentManager_shouldBePendingOffice() {
        OtReportStatus status = workflowService.initialStatus(Role.ROLE_MANAGER_DEPARTMENT);
        assertEquals(OtReportStatus.PENDING_OFFICE, status);
    }

    @Test
    void initialStatus_officeManager_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> workflowService.initialStatus(Role.ROLE_MANAGER_OFFICE));
    }

    @Test
    void nextStatus_departmentManagerApprovePendingDepartment_shouldMoveToPendingOffice() {
        OtReportStatus next = workflowService.nextStatus(
                Role.ROLE_MANAGER_DEPARTMENT,
                OtReportStatus.PENDING_DEPARTMENT,
                true
        );
        assertEquals(OtReportStatus.PENDING_OFFICE, next);
    }

    @Test
    void nextStatus_departmentManagerRejectPending_shouldBeRejected() {
        OtReportStatus next = workflowService.nextStatus(
                Role.ROLE_MANAGER_DEPARTMENT,
                OtReportStatus.PENDING,
                false
        );
        assertEquals(OtReportStatus.REJECTED, next);
    }

    @Test
    void nextStatus_officeManagerApprovePendingOffice_shouldBeApproved() {
        OtReportStatus next = workflowService.nextStatus(
                Role.ROLE_MANAGER_OFFICE,
                OtReportStatus.PENDING_OFFICE,
                true
        );
        assertEquals(OtReportStatus.APPROVED, next);
    }

    @Test
    void nextStatus_officeManagerOnPendingDepartment_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> workflowService.nextStatus(
                Role.ROLE_MANAGER_OFFICE,
                OtReportStatus.PENDING_DEPARTMENT,
                true
        ));
    }

    @Test
    void nextStatus_onTerminalStatus_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> workflowService.nextStatus(
                Role.ROLE_MANAGER_DEPARTMENT,
                OtReportStatus.APPROVED,
                true
        ));
    }

    @Test
    void nextStatus_employeeAsApprover_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class, () -> workflowService.nextStatus(
                Role.ROLE_EMPLOYEE,
                OtReportStatus.PENDING_DEPARTMENT,
                true
        ));
    }

    @Test
    void pendingStatusSets_shouldMatchWorkflowQueues() {
        assertEquals(
                java.util.List.of(OtReportStatus.PENDING_DEPARTMENT, OtReportStatus.PENDING),
                workflowService.pendingForDeptMgr()
        );
        assertEquals(
                java.util.List.of(OtReportStatus.PENDING_OFFICE),
                workflowService.pendingForOfficeMgr()
        );
    }
}


