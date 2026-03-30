package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.department.DepartmentDetailResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentDirectoryResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.department.DepartmentResponse;
import com.deha.HumanResourceManagement.entity.Department;

import java.util.UUID;

public interface IDepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest departmentRequest);

    DepartmentResponse updateDepartment(UUID id, DepartmentRequest departmentRequest);

    void deleteDepartment(UUID id);

    DepartmentResponse getDepartmentById(UUID id);

    DepartmentDetailResponse getDepartmentDetailById(UUID id);

    DepartmentDirectoryResponse getDepartmentDirectory(String keyword, UUID officeId);

    Department findDepartmentById(UUID id);
}

