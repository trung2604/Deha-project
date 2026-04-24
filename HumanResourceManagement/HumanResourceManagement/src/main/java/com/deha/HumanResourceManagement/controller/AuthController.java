package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.auth.*;
import com.deha.HumanResourceManagement.dto.user.UpdateProfileRequest;
import com.deha.HumanResourceManagement.service.support.AuthService;
import com.deha.HumanResourceManagement.service.support.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Authentication, account verification, password recovery and profile self-service APIs")
@RestController
@RequestMapping("/api/auth")
public class AuthController extends ApiControllerSupport {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    public AuthController(AuthService authService, EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Login by email/password", description = "Authenticate user and issue access token with refresh cookie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Account inactive/locked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Authentication session service unavailable")
    })
    public ApiResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return success("Login successful", HttpStatus.OK, authService.login(request, response));
    }

//    @GetMapping("/me")
//    public ApiResponse me(@RequestHeader(value = "Authorization", required = false) String authorization) {
//        return success("User profile retrieved successfully", HttpStatus.OK, authService.me(authorization));
//    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current profile", description = "Return profile information for the authenticated user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse me() {
        return success("User profile retrieved successfully", HttpStatus.OK, authService.me());
    }

    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Refresh access token", description = "Issue a new access token and rotate refresh cookie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ApiResponse refresh(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        return success("Token refreshed successfully", HttpStatus.OK, authService.refresh(refreshToken, response));
    }

    @PostMapping("/oauth2/exchange")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Exchange OAuth2 one-time code", description = "Exchange Google OAuth2 one-time code for access token and refresh cookie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OAuth2 exchange successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired exchange code")
    })
    public ApiResponse exchangeOauth2Code(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        return success("OAuth2 exchange successful", HttpStatus.OK, authService.exchangeOAuth2Code(code, response));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout", description = "Revoke refresh token and clear refresh cookie")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @Operation(summary = "Update current profile", description = "Update profile fields for the authenticated user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Optimistic lock/version conflict")
    })
    public ApiResponse updateProfile(
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        return success("Profile updated successfully", HttpStatus.OK, authService.updateProfile(request));
    }

    @GetMapping("/verify")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Verify account email", description = "Activate account using verification token from email link")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
    })
    public ApiResponse verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return success("Account activated successfully", HttpStatus.OK, null);
    }

    @PostMapping("/forgot-password")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Request forgot-password OTP", description = "Send OTP to email if account exists and is active")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request accepted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ApiResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        emailVerificationService.sendForgotPasswordOtp(request);
        return success("If the email exists, an OTP has been sent", HttpStatus.OK, null);
    }

    @PostMapping("/verify-otp")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Verify forgot-password OTP", description = "Validate OTP and return reset token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "OTP invalid or expired")
    })
    public ApiResponse verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        VerifyOtpResponse response = emailVerificationService.verifyOtp(request);
        return success("OTP verified successfully", HttpStatus.OK, response);
    }

    @PostMapping("/reset-password")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Reset password by reset token", description = "Set new password using reset token from OTP verification")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Reset token invalid/expired or payload invalid")
    })
    public ApiResponse resetPassword(
            @Valid @RequestBody TokenResetPasswordRequest request
    ) {
        emailVerificationService.resetPassword(request);
        return success("Password has been reset successfully", HttpStatus.OK, null);
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Change password for authenticated user and invalidate current refresh session")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Current password invalid or payload invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.changePassword(request, refreshToken, response);
        return success("Password changed successfully", HttpStatus.OK, null);
    }

}

