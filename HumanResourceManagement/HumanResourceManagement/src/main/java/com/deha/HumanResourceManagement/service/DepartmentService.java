package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.department.DepartmentResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponse createDepartment(DepartmentRequest departmentRequest){
        if(departmentRepository.existsByName(departmentRequest.getName())) {
            throw new ResourceAlreadyExistException("Department with the same name already exists.");
        }
        Department department = new Department();
        department.setName(departmentRequest.getName());
        department.setDescription(departmentRequest.getDescription());
        departmentRepository.save(department);
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription());
    }

    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest departmentRequest){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        department.setName(departmentRequest.getName());
        department.setDescription(departmentRequest.getDescription());
        departmentRepository.save(department);
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription());
    }

    public void deleteDepartment(UUID id){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        departmentRepository.delete(department);
    }

    public DepartmentResponse getDepartmentById(UUID id){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription()
        );
    }

    public List<DepartmentResponse> getAllDepartments(){
        return departmentRepository.findAll().stream()
                .map(department -> new DepartmentResponse(
                        department.getId(),
                        department.getName(),
                        department.getDescription()
                ))
                .toList();
    }
}
