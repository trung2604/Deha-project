package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtRequestCreateRequest;
import com.deha.HumanResourceManagement.service.IOtRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ot-requests")
public class OtRequestController extends ApiControllerSupport {
    private final IOtRequestService otRequestService;

    public OtRequestController(IOtRequestService otRequestService) {
        this.otRequestService = otRequestService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OT_REQUEST_APPROVAL_VIEW')")
    public ApiResponse listByApprovalScope() {
        return success("OT requests retrieved successfully", HttpStatus.OK, otRequestService.listByApprovalScope());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER_DEPARTMENT')")
    public ApiResponse create(@Valid @RequestBody OtRequestCreateRequest request) {
        return success("OT request created successfully", HttpStatus.CREATED, otRequestService.create(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER_DEPARTMENT','MANAGER_OFFICE')")
    public ApiResponse listMy() {
        return success("My OT requests retrieved successfully", HttpStatus.OK, otRequestService.listMy());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('OT_REQUEST_APPROVAL_VIEW')")
    public ApiResponse listPendingForScope() {
        return success("Pending OT requests retrieved successfully", HttpStatus.OK, otRequestService.listPendingForScope());
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasAuthority('OT_REQUEST_APPROVE')")
    public ApiResponse decide(@PathVariable UUID id, @Valid @RequestBody OtDecisionRequest request) {
        return success("OT request decision updated successfully", HttpStatus.OK, otRequestService.decide(id, request));
    }
}