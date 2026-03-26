package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {
    boolean existsByNameIgnoreCaseAndOffice_Id(String name, UUID officeId);
    long countByOffice_Id(UUID officeId);
}
