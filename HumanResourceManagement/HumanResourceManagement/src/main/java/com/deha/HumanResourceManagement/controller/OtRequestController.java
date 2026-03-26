package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtRequestCreateRequest;
import com.deha.HumanResourceManagement.service.OtRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ot-requests")
public class OtRequestController extends ApiControllerSupport {
    private final OtRequestService otRequestService;

    public OtRequestController(OtRequestService otRequestService) {
        this.otRequestService = otRequestService;
    }

    @PostMapping
    public ApiResponse create(@Valid @RequestBody OtRequestCreateRequest request) {
        return success("OT request created successfully", HttpStatus.CREATED, otRequestService.create(request));
    }

    @GetMapping("/my")
    public ApiResponse my() {
        return success("My OT requests retrieved successfully", HttpStatus.OK, otRequestService.listMy());
    }

    @GetMapping("/pending")
    public ApiResponse pending() {
        return success("Pending OT requests retrieved successfully", HttpStatus.OK, otRequestService.listPendingInMyOffice());
    }

    @PatchMapping("/{id}/decision")
    public ApiResponse decide(@PathVariable UUID id, @Valid @RequestBody OtDecisionRequest request) {
        return success("OT request decision updated successfully", HttpStatus.OK, otRequestService.decide(id, request));
    }

    
}

