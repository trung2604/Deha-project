package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.payroll.GeneratePayrollRequest;
import com.deha.HumanResourceManagement.dto.payroll.PayrollResponse;

import java.util.List;
import java.util.UUID;

public interface IPayrollService {
    List<PayrollResponse> generate(GeneratePayrollRequest request);

    List<PayrollResponse> listByPeriodAndScope(Integer year, Integer month, UUID officeId);

    PayrollResponse getPayrollDetailById(UUID id);
}

