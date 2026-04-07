package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.auth.LoginRequest;
import com.deha.HumanResourceManagement.dto.user.UpdateProfileRequest;
import com.deha.HumanResourceManagement.service.support.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends ApiControllerSupport {
    private final AuthService authService;

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return success("Login successful", HttpStatus.OK, authService.login(request, response));
    }

//    @GetMapping("/me")
//    public ApiResponse me(@RequestHeader(value = "Authorization", required = false) String authorization) {
//        return success("User profile retrieved successfully", HttpStatus.OK, authService.me(authorization));
//    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse me() {
        return success("User profile retrieved successfully", HttpStatus.OK, authService.me());
    }

    @PostMapping("/refresh")
    public ApiResponse refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        return success("Token refreshed successfully", HttpStatus.OK, authService.refresh(refreshToken, response));
    }

    @PostMapping("/oauth2/exchange")
    public ApiResponse exchangeOauth2Code(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        return success("OAuth2 exchange successful", HttpStatus.OK, authService.exchangeOAuth2Code(code, response));
    }

    @PostMapping("/logout")
    public ApiResponse logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken, response);
        return success("Logged out successfully", HttpStatus.OK, null);
    }

//    @PutMapping("/me")
//    public ApiResponse updateProfile(
//            @RequestHeader(value = "Authorization", required = false) String authorization,
//            @RequestBody @Valid UpdateProfileRequest request
//    ) {
//        return success("Profile updated successfully", HttpStatus.OK, authService.updateProfile(authorization, request));
//    }
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse updateProfile(
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        return success("Profile updated successfully", HttpStatus.OK, authService.updateProfile(request));
    }
    
}

