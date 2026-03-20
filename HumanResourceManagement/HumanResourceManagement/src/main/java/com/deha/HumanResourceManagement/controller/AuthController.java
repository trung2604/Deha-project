package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.auth.LoginRequest;
import com.deha.HumanResourceManagement.dto.auth.LoginResponse;
import com.deha.HumanResourceManagement.dto.auth.UserProfileResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false) // set to true in production HTTPS
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

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        ApiResponse res = new ApiResponse();

        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            res.setMessage("Email and password are required");
            return res;
        }

        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setMessage("Invalid email or password");
            return res;
        }

        if (!user.isActive()) {
            res.setStatus(HttpStatus.FORBIDDEN.value());
            res.setMessage("Account is inactive");
            return res;
        }

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        setRefreshCookie(response, refreshToken);
        res.setStatus(HttpStatus.OK.value());
        res.setMessage("Login successful");
        res.setData(new LoginResponse(token, user.getId(), user.getEmail(), user.getRole()));
        return res;
    }

    @GetMapping("/me")
    public ApiResponse me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        ApiResponse res = new ApiResponse();

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setMessage("Missing or invalid Authorization header");
            return res;
        }

        try {
            String token = authorization.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                res.setMessage("User not found");
                return res;
            }

            res.setStatus(HttpStatus.OK.value());
            res.setMessage("User profile retrieved successfully");
            res.setData(new UserProfileResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole(),
                    user.isActive(),
                    user.getDepartment() != null ? user.getDepartment().getId() : null,
                    user.getDepartment() != null ? user.getDepartment().getName() : null,
                    user.getPosition() != null ? user.getPosition().getId() : null,
                    user.getPosition() != null ? user.getPosition().getName() : null
            ));
            return res;
        } catch (Exception e) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setMessage("Invalid or expired token");
            return res;
        }
    }

    @PostMapping("/refresh")
    public ApiResponse refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        ApiResponse res = new ApiResponse();

        if (refreshToken == null || refreshToken.isBlank()) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setMessage("Missing refresh token");
            return res;
        }

        try {
            String tokenType = jwtUtil.extractTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                res.setMessage("Invalid refresh token");
                clearRefreshCookie(response);
                return res;
            }

            String email = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null || !user.isActive()) {
                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                res.setMessage("Invalid refresh token");
                clearRefreshCookie(response);
                return res;
            }

            String newAccessToken = jwtUtil.generateToken(user.getEmail());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
            setRefreshCookie(response, newRefreshToken); // rotate refresh token

            res.setStatus(HttpStatus.OK.value());
            res.setMessage("Token refreshed successfully");
            res.setData(new LoginResponse(newAccessToken, user.getId(), user.getEmail(), user.getRole()));
            return res;
        } catch (Exception e) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setMessage("Invalid or expired refresh token");
            clearRefreshCookie(response);
            return res;
        }
    }

    @PostMapping("/logout")
    public ApiResponse logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        ApiResponse res = new ApiResponse();
        clearRefreshCookie(response);
        res.setStatus(HttpStatus.OK.value());
        res.setMessage("Logged out successfully");
        return res;
    }
}

