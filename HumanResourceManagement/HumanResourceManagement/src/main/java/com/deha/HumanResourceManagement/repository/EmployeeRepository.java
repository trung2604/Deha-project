package com.deha.HumanResourceManagement.repository;


import com.deha.HumanResourceManagement.entity.Employee;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    boolean existsByEmail(String email);

    Employee findByEmail(@Email(message = "Email is not correct format") String email);
}
