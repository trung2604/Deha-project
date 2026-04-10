package com.deha.HumanResourceManagement.config.security;

import com.deha.HumanResourceManagement.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Data
public class JwtUtil {
    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private static final String REFRESH_PREFIX = "refresh_token:";


    public JwtUtil(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            RedisTemplate<String, String> redisTemplate,
            UserRepository userRepository
    ) {
        this.secretKey = Keys.hmacShaKeyFor(sha256(secretKey));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    private static byte[] sha256(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    public String generateAccessToken(String username, Collection<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("typ", "access")
                .claim("roles", roles == null ? List.of() : roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + token,
                userId.toString(),
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String extractTokenType(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("typ", String.class);
    }

    public UUID getUserIdFromRefreshToken(String token) {
        String value = redisTemplate.opsForValue().get(REFRESH_PREFIX + token);
        if (value == null) {
            throw new JwtException("Refresh token invalid or expired");
        }
        return UUID.fromString(value);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(REFRESH_PREFIX + token);  // fix
    }

}