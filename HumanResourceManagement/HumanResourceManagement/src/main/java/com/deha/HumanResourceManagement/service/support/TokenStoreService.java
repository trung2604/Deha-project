package com.deha.HumanResourceManagement.config.security;

import io.jsonwebtoken.JwtException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenStoreService {

    private static final String VERIFY_PREFIX = "verify_token:";
    private static final String OTP_PREFIX    = "otp:";
    private static final String RESET_PREFIX  = "reset_token:";

    private static final long VERIFY_TTL_HOURS  = 24;
    private static final long OTP_TTL_MINUTES   = 5;
    private static final long RESET_TTL_MINUTES = 15;

    private final RedisTemplate<String, String> redisTemplate;

    public TokenStoreService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ─── Email verification token ─────────────────────────────────────────────

    public String generateVerifyToken(UUID userId) {
        return store(VERIFY_PREFIX, userId.toString(), VERIFY_TTL_HOURS, TimeUnit.HOURS);
    }

    public UUID getUserIdFromVerifyToken(String token) {
        return UUID.fromString(getOrThrow(VERIFY_PREFIX + token, "Verification link is invalid or has expired"));
    }

    public void deleteVerifyToken(String token) {
        redisTemplate.delete(VERIFY_PREFIX + token);
    }


    public String generateOtp(String email) {
        String otp = String.valueOf(new SecureRandom().nextInt(900_000) + 100_000);
        redisTemplate.opsForValue().set(OTP_PREFIX + email, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String stored = redisTemplate.opsForValue().get(OTP_PREFIX + email);
        return stored != null && stored.equals(otp);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_PREFIX + email);
    }

    public String generateResetToken(UUID userId) {
        return store(RESET_PREFIX, userId.toString(), RESET_TTL_MINUTES, TimeUnit.MINUTES);
    }

    public UUID getUserIdFromResetToken(String token) {
        return UUID.fromString(getOrThrow(RESET_PREFIX + token, "Reset token is invalid or has expired"));
    }

    public void deleteResetToken(String token) {
        redisTemplate.delete(RESET_PREFIX + token);
    }

    private String store(String prefix, String value, long ttl, TimeUnit unit) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(prefix + token, value, ttl, unit);
        return token;
    }

    private String getOrThrow(String key, String errorMessage) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) throw new JwtException(errorMessage);
        return value;
    }
}