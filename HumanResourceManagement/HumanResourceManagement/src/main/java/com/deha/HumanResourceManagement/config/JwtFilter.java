package com.deha.HumanResourceManagement.config;

import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                User user = userRepository.findByEmail(username).orElse(null);
                if (user == null || !user.isActive()) {
                    SecurityContextHolder.clearContext();
                } else {
                    // Spring Security's `hasRole("ADMIN")` typically expects `ROLE_ADMIN`,
                    // but rolePrefix/authority mapping can vary. Provide both forms to be safe.
                    String roleName = user.getRole().name();
                    String roleWithPrefix = "ROLE_" + roleName;
                    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
                    authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                    authorities.add(new SimpleGrantedAuthority(roleName));
                    // In case DB already stores "ROLE_ADMIN" style enum/string (defensive)
                    if (roleName.startsWith("ROLE_")) {
                        authorities.add(new SimpleGrantedAuthority(roleName));
                    }

                    var auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // invalid/expired token: skip authentication
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}