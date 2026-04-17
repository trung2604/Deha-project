package com.deha.HumanResourceManagement.mapper.salarycontract;

import com.deha.HumanResourceManagement.entity.SalaryContract;
import com.deha.HumanResourceManagement.entity.User;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class SalaryContractMapperSupport {

    @Named("userId")
    public UUID userId(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("userName")
    public String userName(User user) {
        if (user == null) {
            return null;
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }

    @Named("status")
    public com.deha.HumanResourceManagement.entity.enums.ContractStatus status(SalaryContract contract) {
        if (contract == null) {
            return null;
        }
        return resolveStatus(contract.getStartDate(), contract.getEndDate());
    }

    @Named("statusByDates")
    public com.deha.HumanResourceManagement.entity.enums.ContractStatus statusByDates(LocalDate startDate, LocalDate endDate) {
        return resolveStatus(startDate, endDate);
    }

    private com.deha.HumanResourceManagement.entity.enums.ContractStatus resolveStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (startDate != null && startDate.isAfter(today)) {
            return com.deha.HumanResourceManagement.entity.enums.ContractStatus.FUTURE;
        }
        if (endDate != null && (endDate.isBefore(today) || endDate.isEqual(today))) {
            return com.deha.HumanResourceManagement.entity.enums.ContractStatus.EXPIRED;
        }
        return com.deha.HumanResourceManagement.entity.enums.ContractStatus.ACTIVE;
    }
}

