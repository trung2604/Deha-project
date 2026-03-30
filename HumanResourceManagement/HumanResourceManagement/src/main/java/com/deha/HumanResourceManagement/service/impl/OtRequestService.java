package com.deha.HumanResourceManagement.service.impl;

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
import com.deha.HumanResourceManagement.service.IOtRequestService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import com.deha.HumanResourceManagement.service.ot.workflow.OtRequestWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OtRequestService implements IOtRequestService {
    private final OtRequestRepository otRequestRepository;
    private final AccessScopeService accessScopeService;
    private final OtRequestWorkflowService otRequestWorkflowService;

    public OtRequestService(
            OtRequestRepository otRequestRepository,
            AccessScopeService accessScopeService,
            OtRequestWorkflowService otRequestWorkflowService
    ) {
        this.otRequestRepository = otRequestRepository;
        this.accessScopeService = accessScopeService;
        this.otRequestWorkflowService = otRequestWorkflowService;
    }

    @Override
    @Transactional
    public OtRequestResponse create(OtRequestCreateRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        boolean canCreateOt = accessScopeService.isEmployee(actor) || accessScopeService.isDepartmentManager(actor);
        if (!canCreateOt) throw new ForbiddenException("You do not have permission to create OT request");
        if (actor.getOffice() == null) {
            throw new BadRequestException("User is not assigned to any office");
        }
        LocalDate logDate = request.getLogDate();
        if (logDate == null) {
            throw new BadRequestException("OT date is required");
        }

        if (logDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot create OT request for a past date");
        }
        if (otRequestRepository.findByUserAndLogDate(actor, logDate).isPresent()) {
            throw new BadRequestException("OT request already exists for this date");
        }

        OtRequest entity = new OtRequest();
        entity.setUser(actor);
        entity.setOffice(actor.getOffice());
        entity.setLogDate(logDate);
        entity.setReason(request.getReason().trim());

        entity.setStatus(otRequestWorkflowService.initialStatus(actor.getRole()));
        otRequestRepository.save(entity);
        return OtRequestResponse.fromEntity(entity);
    }

    @Override
    @Transactional
    public OtRequestResponse decide(UUID id, OtDecisionRequest request) {
        OtRequest entity = otRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT request not found"));
        User manager = accessScopeService.currentUserOrThrow();
        boolean approved = Boolean.TRUE.equals(request.getApproved());

        boolean isSelfRequest = entity.getUser() != null
                && entity.getUser().getId() != null
                && entity.getUser().getId().equals(manager.getId());

        if (isSelfRequest && !accessScopeService.isOfficeManager(manager)) {
            throw new ForbiddenException("You cannot approve your own OT request");
        }

        if (accessScopeService.isDepartmentManager(manager)) {
            UUID departmentId = entity.getUser() != null && entity.getUser().getDepartment() != null
                    ? entity.getUser().getDepartment().getId()
                    : null;
            accessScopeService.assertCanManageDepartment(departmentId);
        } else if (accessScopeService.isOfficeManager(manager)) {
            UUID officeId = entity.getOffice() != null ? entity.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(officeId);
        } else {
            throw new ForbiddenException("You do not have permission to decide OT requests");
        }

        entity.setStatus(otRequestWorkflowService.nextStatus(manager.getRole(), entity.getStatus(), approved));

        entity.setApprovedBy(manager);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setDecisionNote(request.getDecisionNote());
        otRequestRepository.save(entity);
        return OtRequestResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtRequestResponse> listMy() {
        User actor = accessScopeService.currentUserOrThrow();
        return otRequestRepository.findByUser_IdOrderByLogDateDesc(actor.getId())
                .stream()
                .map(OtRequestResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtRequestResponse> listPendingForApproverScope() {
        User currentApprover = accessScopeService.currentUserOrThrow();

        if (accessScopeService.isDepartmentManager(currentApprover)) {
            UUID approverDepartmentId = currentApprover.getDepartment() != null ? currentApprover.getDepartment().getId() : null;
            accessScopeService.assertCanManageDepartment(approverDepartmentId);
            List<OtRequest> pendingRequests = new ArrayList<>();
            for (OtRequestStatus status : otRequestWorkflowService.pendingStatusesForDepartmentManager()) {
                pendingRequests.addAll(otRequestRepository.findByUser_Department_IdAndStatusOrderByLogDateDesc(approverDepartmentId, status));
            }
            pendingRequests.sort(Comparator.comparing(OtRequest::getLogDate).reversed());
            return pendingRequests.stream().map(OtRequestResponse::fromEntity).toList();
        }

        if (accessScopeService.isOfficeManager(currentApprover)) {
            UUID approverOfficeId = currentApprover.getOffice() != null ? currentApprover.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(approverOfficeId);
            List<OtRequest> pendingRequests = new ArrayList<>();
            for (OtRequestStatus status : otRequestWorkflowService.pendingStatusesForOfficeManager()) {
                pendingRequests.addAll(otRequestRepository.findByOffice_IdAndStatusOrderByLogDateDesc(approverOfficeId, status));
            }
            pendingRequests.sort(Comparator.comparing(OtRequest::getLogDate).reversed());
            return pendingRequests.stream().map(OtRequestResponse::fromEntity).toList();
        }

        throw new ForbiddenException("You do not have permission to view pending OT requests");
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtRequestResponse> listByApprovalScope() {
        User currentApprover = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isDepartmentManager(currentApprover)) {
            return otRequestRepository
                    .findAllByUser_Department_IdOrderByLogDateDesc(currentApprover.getDepartment().getId())
                    .stream()
                    .map(OtRequestResponse::fromEntity)
                    .toList();
        }
        if (accessScopeService.isOfficeManager(currentApprover)) {
            return otRequestRepository
                    .findAllByOffice_IdOrderByLogDateDesc(currentApprover.getOffice().getId())
                    .stream()
                    .map(OtRequestResponse::fromEntity)
                    .toList();
        }
        return List.of();
    }
}

