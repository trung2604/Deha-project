package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.dto.auth.LoginRequest;
import com.deha.HumanResourceManagement.dto.auth.LoginResponse;
import com.deha.HumanResourceManagement.dto.user.UpdateProfileRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.utils.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthService {
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    @PersistenceContext
    private EntityManager entityManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Email and password are required");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        if (!user.isActive()) {
            throw new ForbiddenException("Account is inactive");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        setRefreshCookie(response, refreshToken);
        return new LoginResponse(accessToken, user.getId(), user.getEmail(), user.getRole());
    }

    public UserResponse me(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7);
        String tokenType = extractTokenTypeOrThrow(token, "Invalid or expired token");
        if (!"access".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }
        String email = extractUsernameOrThrow(token, "Invalid or expired token");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Missing refresh token");
        }

        UUID userId;
        try {
            userId = jwtUtil.validateAndGetUserId(refreshToken);
        } catch (Exception e) {
            clearRefreshCookie(response);
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            clearRefreshCookie(response);
            throw new UnauthorizedException("Invalid refresh token");
        }

        jwtUtil.deleteToken(refreshToken);

        String newAccessToken = jwtUtil.generateToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        setRefreshCookie(response, newRefreshToken);
        return new LoginResponse(newAccessToken, user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional
    public UserResponse updateProfile(String authorizationHeader, UpdateProfileRequest request) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);
        String tokenType = extractTokenTypeOrThrow(token, "Invalid or expired token");
        if (!"access".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }
        String email = extractUsernameOrThrow(token, "Invalid or expired token");
        User current = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

//        assertExpectedVersion(request.getExpectedVersion(), user.getVersion(), "User profile");
        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }

        User user = new User();
        user.setId(current.getId());
        user.setVersion(request.getExpectedVersion());
        user.setEmail(current.getEmail());
        user.setPassword(current.getPassword());
        user.setRole(current.getRole());
        user.setActive(current.isActive());
        user.setCreatedAt(current.getCreatedAt());
        user.setOffice(current.getOffice());
        user.setDepartment(current.getDepartment());
        user.setPosition(current.getPosition());
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        String normalizedPhone = request.getPhone() != null ? request.getPhone().trim() : "";
        user.setPhone(normalizedPhone.isEmpty() ? null : normalizedPhone);
        userRepository.saveAndFlush(user);
        return UserResponse.fromEntity(user);
    }

//    private void assertExpectedVersion(Long expectedVersion, Long currentVersion, String resourceName) {
//        if (expectedVersion == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//        if (!Objects.equals(expectedVersion, currentVersion)) {
//            throw new ConflictException(resourceName + " was modified by another user. Please refresh and retry.");
//        }
//    }

//    private User mergeAndFlush(User user) {
//        if (entityManager != null) {
//            User merged = entityManager.merge(user);
//            entityManager.flush();
//            return merged;
//        }
//        return userRepository.saveAndFlush(user);
//    }

    public void logout(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            jwtUtil.deleteToken(refreshToken);  // xóa khỏi Redis
        }
        clearRefreshCookie(response);
    }
    private String extractUsernameOrThrow(String token, String errorMessage) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            throw new UnauthorizedException(errorMessage);
        }
    }

    private String extractTokenTypeOrThrow(String token, String errorMessage) {
        try {
            return jwtUtil.extractTokenType(token);
        } catch (Exception e) {
            throw new UnauthorizedException(errorMessage);
        }
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(jwtUtil.getRefreshExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

