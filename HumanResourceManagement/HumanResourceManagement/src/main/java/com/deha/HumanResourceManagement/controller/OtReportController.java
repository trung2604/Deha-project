package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportCreateRequest;
import com.deha.HumanResourceManagement.service.IOtReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ot-reports")
public class OtReportController extends ApiControllerSupport {
    private final IOtReportService otReportService;

    public OtReportController(IOtReportService otReportService) {
        this.otReportService = otReportService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER_DEPARTMENT')")
    public ApiResponse create(@Valid @RequestBody OtReportCreateRequest request) {
        return success("OT report created successfully", HttpStatus.CREATED, otReportService.create(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER_DEPARTMENT','MANAGER_OFFICE')")
    public ApiResponse listMy() {
        return success("My OT reports retrieved successfully", HttpStatus.OK, otReportService.listMy());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OT_REPORT_APPROVAL_VIEW')")
    public ApiResponse listByApprovalScope() {
        return success("OT reports retrieved successfully", HttpStatus.OK, otReportService.listByApprovalScope());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('OT_REPORT_APPROVAL_VIEW')")
    public ApiResponse listPendingForScope() {
        return success("Pending OT reports retrieved successfully", HttpStatus.OK, otReportService.listPendingForScope());
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasAuthority('OT_REPORT_APPROVE')")
    public ApiResponse decide(@PathVariable UUID id, @Valid @RequestBody OtDecisionRequest request) {
        return success("OT report decision updated successfully", HttpStatus.OK, otReportService.decide(id, request));
    }
}