package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.department.DepartmentDetailResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            UserRepository userRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
    }

    public DepartmentResponse createDepartment(DepartmentRequest departmentRequest){
        if(departmentRepository.existsByName(departmentRequest.getName())) {
            throw new ResourceAlreadyExistException("Department with the same name already exists.");
        }
        Department department = new Department();
        department.applyDetails(departmentRequest.getName(), departmentRequest.getDescription());
        departmentRepository.save(department);
        return DepartmentResponse.fromEntity(department);
    }

    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest departmentRequest){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        department.applyDetails(departmentRequest.getName(), departmentRequest.getDescription());
        departmentRepository.save(department);
        return DepartmentResponse.fromEntity(department);
    }

    @Transactional
    public void deleteDepartment(UUID id){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        long usersInDept = userRepository.countByDepartment_Id(id);
        if (usersInDept > 0) {
            throw new ConflictException(
                    "Cannot delete department while it still has users assigned. Reassign or remove users first.");
        }
        // Safe order: no users remain, so positions can be removed without FK violations from users.position_id
        positionRepository.deleteAllByDepartmentId(id);
        departmentRepository.delete(department);
    }

    public DepartmentResponse getDepartmentById(UUID id){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentResponse.fromEntity(department);
    }

    @Transactional(readOnly = true)
    public DepartmentDetailResponse getDepartmentDetailById(UUID id) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentDetailResponse.fromEntity(department);
    }

    public List<DepartmentResponse> getAllDepartments(){
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromEntity)
                .toList();
    }

    public Department findDepartmentById(UUID id) {
        return departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
    }
}
