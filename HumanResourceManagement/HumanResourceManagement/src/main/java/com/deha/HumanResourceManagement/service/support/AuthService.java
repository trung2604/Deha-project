package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.config.security.CustomUserDetail;
import com.deha.HumanResourceManagement.dto.auth.ChangePasswordRequest;
import com.deha.HumanResourceManagement.dto.auth.LoginRequest;
import com.deha.HumanResourceManagement.dto.auth.LoginResponse;
import com.deha.HumanResourceManagement.dto.user.UpdateProfileRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.mapper.coreorg.UserMapper;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.config.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String OAUTH2_EXCHANGE_PREFIX = "oauth2_exchange:";

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final boolean cookieSecure;
    private final long oauth2ExchangeCodeTtlMs;
    private final UserMapper userMapper;

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            RedisTemplate<String, String> redisTemplate,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            @Value("${app.oauth2.exchange-code-ttl-ms:60000}") long oauth2ExchangeCodeTtlMs,
            @Value("${app.cookie.secure:false}") boolean cookieSecure
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.oauth2ExchangeCodeTtlMs = oauth2ExchangeCodeTtlMs;
        this.cookieSecure = cookieSecure;
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

        Object principalObj = authentication.getPrincipal();
        if (!(principalObj instanceof CustomUserDetail principal) || principal.getUser() == null) {
            throw new UnauthorizedException("Invalid authentication principal");
        }
        User user = principal.getUser();

        if (!user.isActive()) {
            throw new ForbiddenException("Account is inactive");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                List.of(user.getRole().name())
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        setRefreshCookie(response, refreshToken);
        return new LoginResponse(accessToken, user.getId(), user.getEmail(), user.getRole());
    }

//    public UserResponse me(String authorizationHeader) {
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            throw new UnauthorizedException("Missing or invalid Authorization header");
//        }
//
//        String token = authorizationHeader.substring(7);
//        String tokenType = extractTokenTypeOrThrow(token, "Invalid or expired token");
//        if (!"access".equals(tokenType)) {
//            throw new UnauthorizedException("Invalid token type");
//        }
//        String email = extractUsernameOrThrow("Invalid or expired token");
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        return UserResponse.fromEntity(user);
//    }
    public UserResponse me() {
        String email = currentEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Missing refresh token");
        }

        UUID userId;
        try {
            userId = jwtUtil.getUserIdFromRefreshToken(refreshToken);
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

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                List.of(user.getRole().name())
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        setRefreshCookie(response, newRefreshToken);
        return new LoginResponse(newAccessToken, user.getId(), user.getEmail(), user.getRole());
    }

    public String createOAuth2ExchangeCode(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required for OAuth2 exchange");
        }
        String code = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                OAUTH2_EXCHANGE_PREFIX + code,
                userId.toString(),
                oauth2ExchangeCodeTtlMs,
                TimeUnit.MILLISECONDS
        );
        return code;
    }

    public LoginResponse exchangeOAuth2Code(String code, HttpServletResponse response) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("OAuth2 exchange code is required");
        }

        String key = OAUTH2_EXCHANGE_PREFIX + code;
        String userIdRaw = redisTemplate.opsForValue().get(key);
        if (userIdRaw == null) {
            throw new UnauthorizedException("Invalid or expired OAuth2 exchange code");
        }
        redisTemplate.delete(key);

        UUID userId;
        try {
            userId = UUID.fromString(userIdRaw);
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid OAuth2 exchange code payload");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            throw new ForbiddenException("Account is inactive");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                List.of(user.getRole().name())
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        setRefreshCookie(response, refreshToken);
        return new LoginResponse(accessToken, user.getId(), user.getEmail(), user.getRole());
    }

//    @Transactional
//    public UserResponse updateProfile(String authorizationHeader, UpdateProfileRequest request) {
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            throw new UnauthorizedException("Missing or invalid Authorization header");
//        }
//        String token = authorizationHeader.substring(7);
//        String tokenType = extractTokenTypeOrThrow(token, "Invalid or expired token");
//        if (!"access".equals(tokenType)) {
//            throw new UnauthorizedException("Invalid token type");
//        }
//        String email = extractUsernameOrThrow("Invalid or expired token");
//        User current = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
////        assertExpectedVersion(request.getExpectedVersion(), user.getVersion(), "User profile");
//        if (request.getExpectedVersion() == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//
//        User user = new User();
//        user.setId(current.getId());
//        user.setVersion(request.getExpectedVersion());
//        user.setEmail(current.getEmail());
//        user.setPassword(current.getPassword());
//        user.setRole(current.getRole());
//        user.setActive(current.isActive());
//        user.setCreatedAt(current.getCreatedAt());
//        user.setOffice(current.getOffice());
//        user.setDepartment(current.getDepartment());
//        user.setPosition(current.getPosition());
//        user.setFirstName(request.getFirstName().trim());
//        user.setLastName(request.getLastName().trim());
//        String normalizedPhone = request.getPhone() != null ? request.getPhone().trim() : "";
//        user.setPhone(normalizedPhone.isEmpty() ? null : normalizedPhone);
//        userRepository.saveAndFlush(user);
//        return UserResponse.fromEntity(user);
//    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        String email = currentEmail();
        User current = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }
        if (!request.getExpectedVersion().equals(current.getVersion())) {
            throw new ConflictException("User profile was modified by another user. Please refresh and retry.");
        }
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new BadRequestException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new BadRequestException("Last name is required");
        }

        // Update managed entity instead of replacing it to keep all fields consistent.
        current.setFirstName(request.getFirstName().trim());
        current.setLastName(request.getLastName().trim());
        String normalizedPhone = request.getPhone() != null ? request.getPhone().trim() : "";
        current.setPhone(normalizedPhone.isEmpty() ? null : normalizedPhone);
        userRepository.saveAndFlush(current);
        return userMapper.toResponse(current);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String refreshToken, HttpServletResponse response) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new BadRequestException("Current password is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new BadRequestException("New password is required");
        }
        if (request.getNewPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }

        String email = currentEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.saveAndFlush(user);

        // Invalidate current refresh token to enforce re-login after password change.
        if (refreshToken != null && !refreshToken.isBlank()) {
            jwtUtil.deleteToken(refreshToken);
        }
        clearRefreshCookie(response);
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
    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(jwtUtil.getRefreshExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("Missing authentication context");
        }
        return auth.getName();
    }
}

