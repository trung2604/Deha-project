package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.department.DepartmentDetailResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentEmployeeItem;
import com.deha.HumanResourceManagement.dto.department.DepartmentPositionItem;
import com.deha.HumanResourceManagement.dto.department.DepartmentResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Employee;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public DepartmentService(DepartmentRepository departmentRepository, PositionRepository positionRepository) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
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

    @Transactional
    public void deleteDepartment(UUID id){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        positionRepository.deleteAllByDepartmentId(id);
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

    @Transactional(readOnly = true)
    public DepartmentDetailResponse getDepartmentDetailById(UUID id) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));

        var positions = department.getPositions().stream()
                .map(p -> new DepartmentPositionItem(p.getId(), p.getName()))
                .toList();

        var employees = department.getEmployees().stream()
                .map(e -> toDepartmentEmployeeItem(e))
                .toList();

        return new DepartmentDetailResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                positions,
                employees
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

    public Department findDepartmentById(UUID id) {
        return departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private static DepartmentEmployeeItem toDepartmentEmployeeItem(Employee e) {
        Position p = e.getPosition();
        return new DepartmentEmployeeItem(
                e.getId(),
                e.getFirstName(),
                e.getLastName(),
                e.getEmail(),
                e.getRole(),
                e.isActive(),
                e.getCreatedAt(),
                p != null ? p.getId() : null,
                p != null ? p.getName() : null
        );
    }
}
