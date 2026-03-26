package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportCreateRequest;
import com.deha.HumanResourceManagement.service.OtReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ot-reports")
public class OtReportController extends ApiControllerSupport {
    private final OtReportService otReportService;

    public OtReportController(OtReportService otReportService) {
        this.otReportService = otReportService;
    }

    @PostMapping
    public ApiResponse create(@Valid @RequestBody OtReportCreateRequest request) {
        return success("OT report created successfully", HttpStatus.CREATED, otReportService.create(request));
    }

    @GetMapping("/my")
    public ApiResponse my() {
        return success("My OT reports retrieved successfully", HttpStatus.OK, otReportService.listMy());
    }

    @GetMapping("/pending")
    public ApiResponse pending() {
        return success("Pending OT reports retrieved successfully", HttpStatus.OK, otReportService.listPendingInMyOffice());
    }

    @PatchMapping("/{id}/decision")
    public ApiResponse decide(@PathVariable UUID id, @Valid @RequestBody OtDecisionRequest request) {
        return success("OT report decision updated successfully", HttpStatus.OK, otReportService.decide(id, request));
    }

    
}

