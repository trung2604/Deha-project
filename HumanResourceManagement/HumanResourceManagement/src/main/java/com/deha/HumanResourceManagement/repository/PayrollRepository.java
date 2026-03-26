package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.Payroll;
import com.deha.HumanResourceManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
    Optional<Payroll> findByUserAndPayYearAndPayMonth(User user, Integer payYear, Integer payMonth);

    List<Payroll> findByPayYearAndPayMonthOrderByGeneratedAtDesc(Integer payYear, Integer payMonth);

    List<Payroll> findByOffice_IdAndPayYearAndPayMonthOrderByGeneratedAtDesc(UUID officeId, Integer payYear, Integer payMonth);
}

