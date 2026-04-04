package com.deha.HumanResourceManagement.service.ot.workflow;

import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OtRequestWorkflowService {
    public OtRequestStatus initialStatus(Role requesterRole) {
        if (requesterRole == Role.MANAGER_DEPARTMENT) {
            return OtRequestStatus.PENDING_OFFICE;
        }
        if (requesterRole == Role.EMPLOYEE) {
            return OtRequestStatus.PENDING_DEPARTMENT;
        }
        throw new ForbiddenException("You do not have permission to create OT request");
    }

    public OtRequestStatus nextStatus(Role approverRole, OtRequestStatus currentStatus, boolean approved) {
        if (isTerminal(currentStatus)) {
            throw new BadRequestException("Only pending OT request can be decided");
        }

        if (approverRole == Role.MANAGER_DEPARTMENT) {
            if (currentStatus != OtRequestStatus.PENDING && currentStatus != OtRequestStatus.PENDING_DEPARTMENT) {
                throw new BadRequestException("Department manager can only decide PENDING OT requests");
            }
            return approved ? OtRequestStatus.APPROVED : OtRequestStatus.REJECTED;
        }

        if (approverRole == Role.MANAGER_OFFICE) {
            if (currentStatus != OtRequestStatus.PENDING_OFFICE) {
                throw new BadRequestException("Office manager can only decide PENDING_OFFICE OT requests");
            }
            return approved ? OtRequestStatus.APPROVED : OtRequestStatus.REJECTED;
        }

        throw new ForbiddenException("You do not have permission to decide OT requests");
    }

    public List<OtRequestStatus> pendingForDeptMgr() {
        return List.of(OtRequestStatus.PENDING_DEPARTMENT, OtRequestStatus.PENDING);
    }

    public List<OtRequestStatus> pendingForOfficeMgr() {
        return List.of(OtRequestStatus.PENDING_OFFICE);
    }

    private boolean isTerminal(OtRequestStatus status) {
        return status == OtRequestStatus.APPROVED || status == OtRequestStatus.REJECTED || status == OtRequestStatus.CANCELLED;
    }
}


