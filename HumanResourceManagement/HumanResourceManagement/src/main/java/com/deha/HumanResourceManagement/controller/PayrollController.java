package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.payroll.GeneratePayrollRequest;
import com.deha.HumanResourceManagement.service.IPayrollService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Payroll", description = "Payroll generation and payroll retrieval APIs")
@RestController
@RequestMapping("/api/payrolls")
public class PayrollController extends ApiControllerSupport {
    private final IPayrollService payrollService;

    public PayrollController(IPayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('PAYROLL_GENERATE')")
    public ApiResponse generate(@Valid @RequestBody GeneratePayrollRequest request) {
        return success("Payroll generated successfully", HttpStatus.OK, payrollService.generate(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL_VIEW')")
    public ApiResponse listByPeriodAndScope(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) UUID officeId
    ) {
        return success("Payroll list retrieved successfully", HttpStatus.OK, payrollService.listByPeriodAndScope(year, month, officeId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_VIEW')")
    public ApiResponse getPayrollDetailById(@PathVariable UUID id) {
        return success("Payroll retrieved successfully", HttpStatus.OK, payrollService.getPayrollDetailById(id));
    }

    
}

