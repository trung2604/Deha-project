package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.employee.EmployeeRequest;
import com.deha.HumanResourceManagement.dto.employee.EmployeeResponse;
import com.deha.HumanResourceManagement.dto.employee.UpdateEmployeeRequest;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Employee;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.EmployeeRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    private final PositionRepository positionRepository;


    public EmployeeService(EmployeeRepository employeeRepository, DepartmentService departmentService, PositionRepository positionRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
        this.positionRepository = positionRepository;
    }

    public EmployeeResponse createEmployee(EmployeeRequest employeeRequest) {
        if(employeeRepository.existsByEmail(employeeRequest.getEmail())) {
            throw new ResourceAlreadyExistException("Email already exists");
        }
        UUID departmentId = employeeRequest.getDepartment() != null ? employeeRequest.getDepartment().getId() : null;
        UUID positionId = employeeRequest.getPosition() != null ? employeeRequest.getPosition().getId() : null;
        if (departmentId == null || positionId == null) {
            throw new IllegalArgumentException("Department and Position are required");
        }

        Department department = departmentService.findDepartmentById(departmentId);
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));

        if (position.getDepartment() == null || !departmentId.equals(position.getDepartment().getId())) {
            throw new IllegalArgumentException("Position does not belong to the specified department");
        }

        Employee employee = new Employee();
        employee.setFirstName(employeeRequest.getFirstName());
        employee.setLastName(employeeRequest.getLastName());
        employee.setEmail(employeeRequest.getEmail());
        employee.setRole(employeeRequest.getRole());
        employee.setPassword(employeeRequest.getPassword());
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setCreatedAt(new Date());
        employee.setActive(true);
        employeeRepository.save(employee);
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment().getName(),
                employee.getPosition().getName(),
                employee.getRole(),
                employee.getCreatedAt()
        );
    }

    public EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest employeeRequest) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        UUID departmentId = employeeRequest.getDepartment() != null ? employeeRequest.getDepartment().getId() : null;
        UUID positionId = employeeRequest.getPosition() != null ? employeeRequest.getPosition().getId() : null;
        if (departmentId == null || positionId == null) {
            throw new IllegalArgumentException("Department and Position are required");
        }

        Department department = departmentService.findDepartmentById(departmentId);
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));

        if (position.getDepartment() == null || !departmentId.equals(position.getDepartment().getId())) {
            throw new IllegalArgumentException("Position does not belong to the specified department");
        }

        employee.setFirstName(employeeRequest.getFirstName());
        employee.setLastName(employeeRequest.getLastName());
        employee.setEmail(employeeRequest.getEmail());
        employee.setRole(employeeRequest.getRole());
        employee.setDepartment(department);
        employee.setPosition(position);
        employeeRepository.save(employee);
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment().getName(),
                employee.getPosition().getName(),
                employee.getRole(),
                employee.getCreatedAt()
        );
    }

    public List<EmployeeResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
    }

    public EmployeeResponse getEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return EmployeeResponse.fromEntity(employee);
    }

    public void deleteEmployee(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }
}
