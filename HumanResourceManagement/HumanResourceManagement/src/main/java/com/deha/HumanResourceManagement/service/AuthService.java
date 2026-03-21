package com.deha.HumanResourceManagement.service;

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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Email and password are required");
        }

        User user = userRepository.findByEmail(request.getEmail().trim()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
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
        String email = extractUsernameOrThrow(token, "Invalid or expired token");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Missing refresh token");
        }

        String tokenType = extractTokenTypeOrThrow(refreshToken, "Invalid or expired refresh token");
        if (!"refresh".equals(tokenType)) {
            clearRefreshCookie(response);
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = extractUsernameOrThrow(refreshToken, "Invalid or expired refresh token");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isActive()) {
            clearRefreshCookie(response);
            throw new UnauthorizedException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        setRefreshCookie(response, newRefreshToken);
        return new LoginResponse(newAccessToken, user.getId(), user.getEmail(), user.getRole());
    }

    public UserResponse updateProfile(String authorizationHeader, UpdateProfileRequest request) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);
        String email = extractUsernameOrThrow(token, "Invalid or expired token");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        String normalizedPhone = request.getPhone() != null ? request.getPhone().trim() : "";
        user.setPhone(normalizedPhone.isEmpty() ? null : normalizedPhone);
        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public void logout(HttpServletResponse response) {
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
