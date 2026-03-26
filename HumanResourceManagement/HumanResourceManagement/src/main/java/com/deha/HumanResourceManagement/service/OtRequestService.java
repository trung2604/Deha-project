package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtRequestCreateRequest;
import com.deha.HumanResourceManagement.dto.ot.OtRequestResponse;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OtRequestService {
    private static final LocalTime OT_START_TIME = LocalTime.of(18, 0);

    private final OtRequestRepository otRequestRepository;
    private final AccessScopeService accessScopeService;

    public OtRequestService(OtRequestRepository otRequestRepository, AccessScopeService accessScopeService) {
        this.otRequestRepository = otRequestRepository;
        this.accessScopeService = accessScopeService;
    }

    @Transactional
    public OtRequestResponse create(OtRequestCreateRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        if (actor.getOffice() == null) {
            throw new BadRequestException("User is not assigned to any office");
        }
        LocalDate logDate = request.getLogDate();
        if (logDate == null) {
            throw new BadRequestException("OT date is required");
        }
        if (!logDate.isAfter(LocalDate.now()) && !logDate.isEqual(LocalDate.now())) {
            throw new BadRequestException("OT request must be created before or on OT day");
        }
        if (logDate.isEqual(LocalDate.now()) && !LocalTime.now().isBefore(OT_START_TIME)) {
            throw new BadRequestException("OT request for today must be created before 18:00");
        }
        if (otRequestRepository.findByUserAndLogDate(actor, logDate).isPresent()) {
            throw new BadRequestException("OT request already exists for this date");
        }

        OtRequest entity = new OtRequest();
        entity.setUser(actor);
        entity.setOffice(actor.getOffice());
        entity.setLogDate(logDate);
        entity.setReason(request.getReason().trim());
        // Approval stage depends on creator role:
        // - Department manager creating OT should skip department-stage and go directly to office-stage.
        // - Office manager creating OT can self-approve at office-stage.
        // - Everyone else goes through department-stage first.
        boolean skipDepartmentStage = accessScopeService.isDepartmentManager(actor)
                || accessScopeService.isOfficeManager(actor)
                || accessScopeService.isAdmin(actor);
        entity.setStatus(skipDepartmentStage ? OtRequestStatus.PENDING_OFFICE : OtRequestStatus.PENDING_DEPARTMENT);
        otRequestRepository.save(entity);
        return OtRequestResponse.fromEntity(entity);
    }

    @Transactional
    public OtRequestResponse decide(UUID id, OtDecisionRequest request) {
        OtRequest entity = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT request not found"));
        User manager = accessScopeService.currentUserOrThrow();
        boolean approved = Boolean.TRUE.equals(request.getApproved());

        // Self-approval rules:
        // - Office manager is allowed to self-approve their own OT request.
        // - Everyone else cannot approve their own OT request.
        if (entity.getUser() != null
                && entity.getUser().getId() != null
                && entity.getUser().getId().equals(manager.getId())
                && !accessScopeService.isOfficeManager(manager)
                && !accessScopeService.isAdmin(manager)) {
            throw new ForbiddenException("You cannot approve your own OT request");
        }

        OtRequestStatus status = entity.getStatus();
        if (status == OtRequestStatus.APPROVED || status == OtRequestStatus.REJECTED) {
            throw new BadRequestException("Only pending OT request can be decided");
        }

        if (accessScopeService.isAdmin(manager)) {
            // Admin can approve/reject at any pending stage.
            if (!(status == OtRequestStatus.PENDING || status == OtRequestStatus.PENDING_DEPARTMENT || status == OtRequestStatus.PENDING_OFFICE)) {
                throw new BadRequestException("Only pending OT request can be decided");
            }
            entity.setStatus(approved ? OtRequestStatus.APPROVED : OtRequestStatus.REJECTED);
        } else if (accessScopeService.isDepartmentManager(manager)) {
            // Department manager: only approve the first stage.
            UUID departmentId = entity.getUser() != null && entity.getUser().getDepartment() != null
                    ? entity.getUser().getDepartment().getId()
                    : null;
            accessScopeService.assertCanManageDepartment(departmentId);

            if (!(status == OtRequestStatus.PENDING || status == OtRequestStatus.PENDING_DEPARTMENT)) {
                throw new BadRequestException("Department manager can only decide PENDING OT requests");
            }
            entity.setStatus(approved ? OtRequestStatus.PENDING_OFFICE : OtRequestStatus.REJECTED);
        } else if (accessScopeService.isOfficeManager(manager)) {
            // Office manager: only approve the second stage.
            UUID officeId = entity.getOffice() != null ? entity.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(officeId);

            if (status != OtRequestStatus.PENDING_OFFICE) {
                throw new BadRequestException("Office manager can only decide PENDING_OFFICE OT requests");
            }
            entity.setStatus(approved ? OtRequestStatus.APPROVED : OtRequestStatus.REJECTED);
        } else {
            throw new ForbiddenException("You do not have permission to decide OT requests");
        }

        entity.setApprovedBy(manager);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setDecisionNote(request.getDecisionNote());
        otRequestRepository.save(entity);
        return OtRequestResponse.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public List<OtRequestResponse> listMy() {
        User actor = accessScopeService.currentUserOrThrow();
        return otRequestRepository.findByUser_IdOrderByLogDateDesc(actor.getId())
                .stream()
                .map(OtRequestResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OtRequestResponse> listPendingInMyOffice() {
        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isDepartmentManager(actor)) {
            UUID departmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;
            accessScopeService.assertCanManageDepartment(departmentId);
            // Backward compatibility: legacy status `PENDING` behaves like `PENDING_DEPARTMENT`.
            List<OtRequest> rows = new ArrayList<>();
            rows.addAll(otRequestRepository.findByUser_Department_IdAndStatusOrderByLogDateDesc(departmentId, OtRequestStatus.PENDING_DEPARTMENT));
            rows.addAll(otRequestRepository.findByUser_Department_IdAndStatusOrderByLogDateDesc(departmentId, OtRequestStatus.PENDING));
            // Department manager can "track" both stages in their department.
            rows.addAll(otRequestRepository.findByUser_Department_IdAndStatusOrderByLogDateDesc(departmentId, OtRequestStatus.PENDING_OFFICE));
            rows.sort(Comparator.comparing(OtRequest::getLogDate).reversed());
            return rows.stream().map(OtRequestResponse::fromEntity).toList();
        }

        if (accessScopeService.isOfficeManager(actor)) {
            UUID officeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(officeId);
            return otRequestRepository.findByOffice_IdAndStatusOrderByLogDateDesc(officeId, OtRequestStatus.PENDING_OFFICE)
                    .stream()
                    .map(OtRequestResponse::fromEntity)
                    .toList();
        }

        // Admin: show all pending queues (both stages).
        List<OtRequest> rows = new ArrayList<>();
        rows.addAll(otRequestRepository.findByStatusOrderByLogDateDesc(OtRequestStatus.PENDING_DEPARTMENT));
        rows.addAll(otRequestRepository.findByStatusOrderByLogDateDesc(OtRequestStatus.PENDING));
        rows.addAll(otRequestRepository.findByStatusOrderByLogDateDesc(OtRequestStatus.PENDING_OFFICE));
        rows.sort(Comparator.comparing(OtRequest::getLogDate).reversed());
        return rows.stream().map(OtRequestResponse::fromEntity).toList();
    }
}

