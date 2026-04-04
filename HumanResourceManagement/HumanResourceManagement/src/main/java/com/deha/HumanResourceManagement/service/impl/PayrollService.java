package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.payroll.GeneratePayrollRequest;
import com.deha.HumanResourceManagement.dto.payroll.PayrollResponse;
import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.Payroll;
import com.deha.HumanResourceManagement.entity.SalaryContract;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import com.deha.HumanResourceManagement.entity.enums.PayrollStatus;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.PayrollRepository;
import com.deha.HumanResourceManagement.repository.OtReportRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.IAttendanceService;
import com.deha.HumanResourceManagement.service.IPayrollService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import com.deha.HumanResourceManagement.service.payroll.PayrollCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PayrollService implements IPayrollService {
    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final OtReportRepository otReportRepository;
    private final IAttendanceService attendanceService;
    private final SalaryContractService salaryContractService;
    private final AccessScopeService accessScopeService;
    private final PayrollCalculator payrollCalculator;
    private final OfficePolicyService officePolicyService;

    public PayrollService(
            PayrollRepository payrollRepository,
            UserRepository userRepository,
            AttendanceLogRepository attendanceLogRepository,
            OtReportRepository otReportRepository,
            IAttendanceService attendanceService,
            SalaryContractService salaryContractService,
            AccessScopeService accessScopeService,
            PayrollCalculator payrollCalculator
            , OfficePolicyService officePolicyService
    ) {
        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.otReportRepository = otReportRepository;
        this.attendanceService = attendanceService;
        this.salaryContractService = salaryContractService;
        this.accessScopeService = accessScopeService;
        this.payrollCalculator = payrollCalculator;
        this.officePolicyService = officePolicyService;
    }

    @Override
    @Transactional
    public List<PayrollResponse> generate(GeneratePayrollRequest request) {
        validatePeriod(request.getYear(), request.getMonth());

        YearMonth yearMonth = YearMonth.of(request.getYear(), request.getMonth());
        LocalDate periodStart = yearMonth.atDay(1);
        LocalDate periodEnd = yearMonth.atEndOfMonth();

        List<User> targets = resolveTargets(request);
        List<PayrollResponse> results = new ArrayList<>();
        for (User user : targets) {
            if (!user.isActive()) continue;
            if (user.getOffice() == null) continue;

            SalaryContract contract = salaryContractService.findActiveContractOrNull(user, periodStart, periodEnd);
            if (contract == null) continue;

            List<AttendanceLog> logs = attendanceLogRepository.findByUserAndLogDateBetween(user, periodStart, periodEnd);
            logs.forEach(attendanceService::synchronizeDerivedFields);
            int workingDaysInMonth = payrollCalculator.countWorkingDays(periodStart, periodEnd);
            int presentDays = payrollCalculator.countPresentDays(logs);
            int regularHours = payrollCalculator.sumRegularHours(logs);
            List<OtReport> approvedOtReports = otReportRepository.findByAttendanceLog_User_IdAndAttendanceLog_LogDateBetweenAndStatus(
                    user.getId(),
                    periodStart,
                    periodEnd,
                    OtReportStatus.APPROVED
            );
            int otHours = approvedOtReports.stream().mapToInt(r -> r.getReportedOtHours() == null ? 0 : r.getReportedOtHours()).sum();
            int standardWorkHours = officePolicyService.standardWorkHours(user.getOffice());
            double otWeekdayMultiplier = officePolicyService.otWeekdayMultiplier(user.getOffice());
            double otWeekendMultiplier = officePolicyService.otWeekendMultiplier(user.getOffice());
            double otHolidayMultiplier = officePolicyService.otHolidayMultiplier(user.getOffice());
            double otNightBonusMultiplier = officePolicyService.otNightBonusMultiplier(user.getOffice());

            BigDecimal regularPay = payrollCalculator.calculateRegularPay(
                    contract.getBaseSalary(), regularHours, workingDaysInMonth, standardWorkHours
            );
            BigDecimal otPay = payrollCalculator.calculateOtPay(
                    approvedOtReports,
                    contract.getBaseSalary(),
                    workingDaysInMonth,
                    standardWorkHours,
                    otWeekdayMultiplier,
                    otWeekendMultiplier,
                    otHolidayMultiplier,
                    otNightBonusMultiplier
            );
            BigDecimal netSalary = regularPay.add(otPay);

            Payroll payroll = payrollRepository.findByUserAndPayYearAndPayMonth(user, request.getYear(), request.getMonth())
                    .orElseGet(Payroll::new);
            payroll.setUser(user);
            payroll.setOffice(user.getOffice());
            payroll.setPayYear(request.getYear());
            payroll.setPayMonth(request.getMonth());
            payroll.setBaseSalarySnapshot(contract.getBaseSalary());
            payroll.setWorkingDaysInMonth(workingDaysInMonth);
            payroll.setPresentDays(presentDays);
            payroll.setRegularHours(regularHours);
            payroll.setRegularPay(regularPay);
            payroll.setOtHours(otHours);
            payroll.setOtPay(otPay);
            payroll.setNetSalary(netSalary);
            payroll.setStatus(PayrollStatus.DRAFT);
            payroll.setGeneratedAt(LocalDateTime.now());
            payrollRepository.save(payroll);

            results.add(PayrollResponse.fromEntity(payroll));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> listByPeriodAndScope(Integer year, Integer month, UUID officeId) {
        validatePeriod(year, month);

        List<Payroll> payrolls;
        if (officeId != null) {
            payrolls = payrollRepository.findByOffice_IdAndPayYearAndPayMonthOrderByGeneratedAtDesc(officeId, year, month);
        } else {
            User actor = accessScopeService.currentUserOrThrow();
            if (accessScopeService.isAdmin(actor)) {
                payrolls = payrollRepository.findByPayYearAndPayMonthOrderByGeneratedAtDesc(year, month);
            } else {
                UUID actorOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
                accessScopeService.assertCanManageOffice(actorOfficeId);
                payrolls = payrollRepository.findByOffice_IdAndPayYearAndPayMonthOrderByGeneratedAtDesc(actorOfficeId, year, month);
            }
        }

        return payrolls.stream().map(PayrollResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollDetailById(UUID id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));
        UUID officeId = payroll.getOffice() != null ? payroll.getOffice().getId() : null;
        accessScopeService.assertCanManageOffice(officeId);
        return PayrollResponse.fromEntity(payroll);
    }

    private List<User> resolveTargets(GeneratePayrollRequest request) {
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            UUID officeId = user.getOffice() != null ? user.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(officeId);
            return List.of(user);
        }

        if (request.getOfficeId() != null) {
            accessScopeService.assertCanManageOffice(request.getOfficeId());
            return userRepository.findByOffice_Id(request.getOfficeId());
        }

        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isAdmin(actor)) {
            return userRepository.findAll();
        }

        UUID officeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
        accessScopeService.assertCanManageOffice(officeId);
        return userRepository.findByOffice_Id(officeId);
    }

    private void validatePeriod(Integer year, Integer month) {
        if (year == null || year < 2000 || year > 2100) {
            throw new BadRequestException("Year must be between 2000 and 2100");
        }
        if (month == null || month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
    }
}


