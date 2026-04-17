package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.office.OfficePolicyRequest;
import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.service.IOfficeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Offices", description = "Office management and office policy APIs")
@RestController
@RequestMapping("/api/offices")
public class OfficeController extends ApiControllerSupport {
    private final IOfficeService officeService;

    public OfficeController(IOfficeService officeService) {
        this.officeService = officeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    public ApiResponse getAll() {
        return success("Offices retrieved successfully", HttpStatus.OK, officeService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@RequestBody @Valid OfficeRequest request) {
        return success("Office created successfully", HttpStatus.CREATED, officeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable UUID id, @RequestBody @Valid OfficeRequest request) {
        return success("Office updated successfully", HttpStatus.OK, officeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable UUID id) {
        officeService.delete(id);
        return success("Office deleted successfully", HttpStatus.OK, null);
    }

    @GetMapping("/my-policy")
    @PreAuthorize("hasRole('MANAGER_OFFICE')")
    public ApiResponse myPolicy() {
        return success("Office policy retrieved successfully", HttpStatus.OK, officeService.getMyPolicy());
    }

    @PutMapping("/my-policy")
    @PreAuthorize("hasRole('MANAGER_OFFICE')")
    public ApiResponse updateMyPolicy(@RequestBody @Valid OfficePolicyRequest request) {
        return success("Office policy updated successfully", HttpStatus.OK, officeService.updateMyPolicy(request));
    }
}
