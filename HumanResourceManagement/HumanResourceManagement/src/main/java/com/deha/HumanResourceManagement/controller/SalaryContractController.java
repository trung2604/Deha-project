package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.salarycontract.SalaryContractRequest;
import com.deha.HumanResourceManagement.service.impl.SalaryContractService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Salary Contracts", description = "Salary contract management APIs")
@RestController
@RequestMapping("/api/salary-contracts")
public class SalaryContractController extends ApiControllerSupport {
    private final SalaryContractService salaryContractService;

    public SalaryContractController(SalaryContractService salaryContractService) {
        this.salaryContractService = salaryContractService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    public ApiResponse create(@Valid @RequestBody SalaryContractRequest request) {
        return success("Salary contract created successfully", HttpStatus.CREATED, salaryContractService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    public ApiResponse update(@PathVariable UUID id, @Valid @RequestBody SalaryContractRequest request) {
        return success("Salary contract updated successfully", HttpStatus.OK, salaryContractService.update(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    public ApiResponse listByUser(@RequestParam UUID userId) {
        return success("Salary contracts retrieved successfully", HttpStatus.OK, salaryContractService.getByUser(userId));
    }

    
}
