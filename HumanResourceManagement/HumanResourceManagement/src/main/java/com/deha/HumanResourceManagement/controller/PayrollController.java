package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.payroll.GeneratePayrollRequest;
import com.deha.HumanResourceManagement.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payrolls")
public class PayrollController extends ApiControllerSupport {
    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/generate")
    public ApiResponse generate(@Valid @RequestBody GeneratePayrollRequest request) {
        return success("Payroll generated successfully", HttpStatus.OK, payrollService.generate(request));
    }

    @GetMapping
    public ApiResponse list(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) UUID officeId
    ) {
        return success("Payroll list retrieved successfully", HttpStatus.OK, payrollService.list(year, month, officeId));
    }

    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable UUID id) {
        return success("Payroll retrieved successfully", HttpStatus.OK, payrollService.getById(id));
    }

    
}

