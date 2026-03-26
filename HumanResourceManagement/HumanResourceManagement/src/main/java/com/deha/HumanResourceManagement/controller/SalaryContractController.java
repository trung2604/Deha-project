package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.salarycontract.SalaryContractRequest;
import com.deha.HumanResourceManagement.service.SalaryContractService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/salary-contracts")
public class SalaryContractController extends ApiControllerSupport {
    private final SalaryContractService salaryContractService;

    public SalaryContractController(SalaryContractService salaryContractService) {
        this.salaryContractService = salaryContractService;
    }

    @PostMapping
    public ApiResponse create(@Valid @RequestBody SalaryContractRequest request) {
        return success("Salary contract created successfully", HttpStatus.CREATED, salaryContractService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable UUID id, @Valid @RequestBody SalaryContractRequest request) {
        return success("Salary contract updated successfully", HttpStatus.OK, salaryContractService.update(id, request));
    }

    @GetMapping
    public ApiResponse listByUser(@RequestParam UUID userId) {
        return success("Salary contracts retrieved successfully", HttpStatus.OK, salaryContractService.getByUser(userId));
    }

    
}

