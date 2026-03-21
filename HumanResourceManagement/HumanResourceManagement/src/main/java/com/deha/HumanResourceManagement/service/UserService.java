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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        Department department = departmentService.findDepartmentById(departmentId);
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));

        User user = new User();
        user.applyBasicInfo(
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getEmail(),
                userRequest.getRole()
        );
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.assignDepartmentAndPosition(department, position);
        user.markCreatedNow();
        user.activate();
        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUser(UUID id, UpdateUserRequest UserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UUID departmentId = UserRequest.getDepartment() != null ? UserRequest.getDepartment().getId() : null;
        UUID positionId = UserRequest.getPosition() != null ? UserRequest.getPosition().getId() : null;

        Department department = departmentService.findDepartmentById(departmentId);
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));

        user.applyBasicInfo(
                UserRequest.getFirstName(),
                UserRequest.getLastName(),
                UserRequest.getEmail(),
                UserRequest.getRole()
        );
        user.assignDepartmentAndPosition(department, position);
        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.searchUsers(null, null, null, null, pageable);
        return users.map(UserResponse::fromEntity);
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

    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        Page<User> users = userRepository.searchUsers(normalizedKeyword, null, null, null, pageable);
        return users.map(UserResponse::fromEntity);
    }

    public Page<UserResponse> getUsersWithFilters(String keyword, UUID departmentId, UUID positionId, Boolean active, Pageable pageable) {
        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        Page<User> users = userRepository.searchUsers(normalizedKeyword, departmentId, positionId, active, pageable);
        return users.map(UserResponse::fromEntity);
    }
}
