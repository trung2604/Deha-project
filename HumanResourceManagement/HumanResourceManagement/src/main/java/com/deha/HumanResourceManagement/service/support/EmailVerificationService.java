package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.config.security.TokenStoreService;
import com.deha.HumanResourceManagement.dto.auth.ForgotPasswordRequest;
import com.deha.HumanResourceManagement.dto.auth.TokenResetPasswordRequest;
import com.deha.HumanResourceManagement.dto.auth.VerifyOtpRequest;
import com.deha.HumanResourceManagement.dto.auth.VerifyOtpResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final TokenStoreService tokenStoreService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public EmailVerificationService(
            UserRepository userRepository,
            TokenStoreService tokenStoreService,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.tokenStoreService = tokenStoreService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void sendVerificationEmail(User user) {
        String token = tokenStoreService.generateVerifyToken(user.getId());
        String fullName = ((user.getFirstName() != null ? user.getFirstName().trim() : "") + " "
                + (user.getLastName() != null ? user.getLastName().trim() : "")).trim();
        emailService.sendVerificationEmail(user.getEmail(), fullName, token);
    }

    @Transactional
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Verification token is required");
        }
        UUID userId;
        try {
            userId = tokenStoreService.getUserIdFromVerifyToken(token);
        } catch (JwtException e) {
            throw new BadRequestException("Verification link is invalid or has expired");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            user.setActive(true);
            userRepository.save(user);
        }
        tokenStoreService.deleteVerifyToken(token);
    }

    public void sendForgotPasswordOtp(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (!user.isActive()) return;
            String otp = tokenStoreService.generateOtp(user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), otp);
        });
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        if (!tokenStoreService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new BadRequestException("OTP is incorrect or has expired");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        tokenStoreService.deleteOtp(user.getEmail());
        String resetToken = tokenStoreService.generateResetToken(user.getId());
        return new VerifyOtpResponse(resetToken);
    }

    @Transactional
    public void resetPassword(TokenResetPasswordRequest request) {
        UUID userId;
        try {
            userId = tokenStoreService.getUserIdFromResetToken(request.getResetToken());
        } catch (JwtException e) {
            throw new BadRequestException("Reset token is invalid or has expired");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenStoreService.deleteResetToken(request.getResetToken());
    }
}