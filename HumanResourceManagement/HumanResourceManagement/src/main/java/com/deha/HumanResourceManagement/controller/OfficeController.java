package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.office.OfficePolicyRequest;
import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.service.IOfficeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/offices")
public class OfficeController extends ApiControllerSupport {
    private final IOfficeService officeService;

    public OfficeController(IOfficeService officeService) {
        this.officeService = officeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OFFICE_VIEW')")
    public ApiResponse getAll() {
        return success("Offices retrieved successfully", HttpStatus.OK, officeService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('OFFICE_MANAGE')")
    public ApiResponse create(@RequestBody @Valid OfficeRequest request) {
        return success("Office created successfully", HttpStatus.CREATED, officeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFICE_MANAGE')")
    public ApiResponse update(@PathVariable UUID id, @RequestBody @Valid OfficeRequest request) {
        return success("Office updated successfully", HttpStatus.OK, officeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFICE_MANAGE')")
    public ApiResponse delete(@PathVariable UUID id) {
        officeService.delete(id);
        return success("Office deleted successfully", HttpStatus.OK, null);
    }

    @GetMapping("/my-policy")
    @PreAuthorize("hasAuthority('OFFICE_POLICY_VIEW')")
    public ApiResponse myPolicy() {
        return success("Office policy retrieved successfully", HttpStatus.OK, officeService.getMyPolicy());
    }

    @PutMapping("/my-policy")
    @PreAuthorize("hasAuthority('OFFICE_POLICY_UPDATE')")
    public ApiResponse updateMyPolicy(@RequestBody @Valid OfficePolicyRequest request) {
        return success("Office policy updated successfully", HttpStatus.OK, officeService.updateMyPolicy(request));
    }
}
