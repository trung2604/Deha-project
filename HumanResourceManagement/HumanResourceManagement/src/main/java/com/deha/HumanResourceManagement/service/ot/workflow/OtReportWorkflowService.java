package com.deha.HumanResourceManagement.service.ot.workflow;

import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OtReportWorkflowService {
    public OtReportStatus initialStatus(Role requesterRole) {
        if (requesterRole == Role.ROLE_MANAGER_DEPARTMENT) {
            return OtReportStatus.PENDING_OFFICE;
        }
        if (requesterRole == Role.ROLE_EMPLOYEE) {
            return OtReportStatus.PENDING_DEPARTMENT;
        }
        throw new ForbiddenException("You do not have permission to submit OT report");
    }

    public OtReportStatus nextStatus(Role approverRole, OtReportStatus currentStatus, boolean approved) {
        if (isTerminal(currentStatus)) {
            throw new BadRequestException("Only pending OT report can be decided");
        }

        if (approverRole == Role.ROLE_MANAGER_DEPARTMENT) {
            if (currentStatus != OtReportStatus.PENDING && currentStatus != OtReportStatus.PENDING_DEPARTMENT) {
                throw new BadRequestException("Department manager can only decide PENDING OT reports");
            }
            return approved ? OtReportStatus.PENDING_OFFICE : OtReportStatus.REJECTED;
        }

        if (approverRole == Role.ROLE_MANAGER_OFFICE) {
            if (currentStatus != OtReportStatus.PENDING_OFFICE) {
                throw new BadRequestException("Office manager can only decide PENDING_OFFICE OT reports");
            }
            return approved ? OtReportStatus.APPROVED : OtReportStatus.REJECTED;
        }

        throw new ForbiddenException("You do not have permission to decide OT reports");
    }

    public List<OtReportStatus> pendingStatusesForDepartmentManager() {
        return List.of(OtReportStatus.PENDING_DEPARTMENT, OtReportStatus.PENDING);
    }

    public List<OtReportStatus> pendingStatusesForOfficeManager() {
        return List.of(OtReportStatus.PENDING_OFFICE);
    }

    private boolean isTerminal(OtReportStatus status) {
        return status == OtReportStatus.APPROVED || status == OtReportStatus.REJECTED;
    }
}


