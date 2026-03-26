package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.SalaryContract;
import com.deha.HumanResourceManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalaryContractRepository extends JpaRepository<SalaryContract, UUID> {
    List<SalaryContract> findByUserOrderByStartDateDesc(User user);
}

