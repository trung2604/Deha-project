package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.user.UserRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.dto.user.UpdateUserRequest;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, DepartmentService departmentService, PositionRepository positionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentService = departmentService;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserRequest userRequest) {
        if(userRepository.existsByEmail(userRequest.getEmail())) {
            throw new ResourceAlreadyExistException("Email already exists");
        }
        UUID departmentId = userRequest.getDepartment() != null ? userRequest.getDepartment().getId() : null;
        UUID positionId = userRequest.getPosition() != null ? userRequest.getPosition().getId() : null;
        if (departmentId == null || positionId == null) {
            throw new IllegalArgumentException("Department and Position are required");
        }

        Department department = departmentService.findDepartmentById(departmentId);
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));

        if (position.getDepartment() == null || !departmentId.equals(position.getDepartment().getId())) {
            throw new IllegalArgumentException("Position does not belong to the specified department");
        }

        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setRole(userRequest.getRole());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setDepartment(department);
        user.setPosition(position);
        user.setCreatedAt(new Date());
        user.setActive(true);
        userRepository.save(user);
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment().getName(),
                user.getPosition() != null ? user.getPosition().getId() : null,
                user.getPosition().getName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    public UserResponse updateUser(UUID id, UpdateUserRequest UserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UUID departmentId = UserRequest.getDepartment() != null ? UserRequest.getDepartment().getId() : null;
        UUID positionId = UserRequest.getPosition() != null ? UserRequest.getPosition().getId() : null;
        if (departmentId == null || positionId == null) {
            throw new IllegalArgumentException("Department and Position are required");
        }

        Department department = departmentService.findDepartmentById(departmentId);
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));

        if (position.getDepartment() == null || !departmentId.equals(position.getDepartment().getId())) {
            throw new IllegalArgumentException("Position does not belong to the specified department");
        }

        user.setFirstName(UserRequest.getFirstName());
        user.setLastName(UserRequest.getLastName());
        user.setEmail(UserRequest.getEmail());
        user.setRole(UserRequest.getRole());
        user.setDepartment(department);
        user.setPosition(position);
        userRepository.save(user);
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment().getName(),
                user.getPosition() != null ? user.getPosition().getId() : null,
                user.getPosition().getName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
