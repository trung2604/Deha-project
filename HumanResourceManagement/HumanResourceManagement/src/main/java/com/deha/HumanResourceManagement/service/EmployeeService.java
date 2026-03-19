package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.EmployeeRequest;
import com.deha.HumanResourceManagement.dto.EmployeeResponse;
import com.deha.HumanResourceManagement.dto.UpdateEmployeeRequest;
import com.deha.HumanResourceManagement.entity.Employee;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeResponse createEmployee(EmployeeRequest employeeRequest) {
        if(employeeRepository.existsByEmail(employeeRequest.getEmail())) {
            throw new ResourceAlreadyExistException("Email already exists");
        }
        if(employeeRequest.getPosition().getDepartment().getId() != employeeRequest.getDepartment().getId()){
            throw new IllegalArgumentException("Position does not belong to the specified department");
        }
        Employee employee = new Employee();
        employee.setFirstName(employeeRequest.getFirstName());
        employee.setLastName(employeeRequest.getLastName());
        employee.setEmail(employeeRequest.getEmail());
        employee.setRole(employeeRequest.getRole());
        employee.setPassword(employeeRequest.getPassword());
        employee.setDepartment(employeeRequest.getDepartment());
        employee.setPosition(employeeRequest.getPosition());
        employee.setCreatedAt(new Date());
        employee.setActive(true);
        employeeRepository.save(employee);
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getPosition(),
                employee.getRole(),
                employee.getCreatedAt()
        );
    }

    public EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest employeeRequest) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if(employeeRequest.getPosition().getDepartment().getId() != employeeRequest.getDepartment().getId()){
            throw new IllegalArgumentException("Position does not belong to the specified department");
        }
        employee.setFirstName(employeeRequest.getFirstName());
        employee.setLastName(employeeRequest.getLastName());
        employee.setEmail(employeeRequest.getEmail());
        employee.setRole(employeeRequest.getRole());
        employeeRepository.save(employee);
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getPosition(),
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
