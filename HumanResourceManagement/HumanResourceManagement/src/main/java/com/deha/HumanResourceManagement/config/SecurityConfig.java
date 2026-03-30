package com.deha.HumanResourceManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC
                        .requestMatchers("/api/auth/**").permitAll()
                        // OFFICE
                        .requestMatchers("/api/offices/my-policy")
                        .hasRole("MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.GET, "/api/offices/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers("/api/offices/**")
                        .hasRole("ADMIN")
                        // USERS
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        // DEPARTMENTS
                        .requestMatchers(HttpMethod.GET, "/api/departments/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.POST, "/api/departments/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.PUT, "/api/departments/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.DELETE, "/api/departments/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        // POSITIONS
                        .requestMatchers(HttpMethod.GET, "/api/positions")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers("/api/positions/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        // SALARY & PAYROLL
                        .requestMatchers("/api/salary-contracts/**", "/api/payrolls/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        // ATTENDANCE
                        .requestMatchers("/api/attendance/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT", "EMPLOYEE")
                        // OT
                        .requestMatchers(HttpMethod.POST, "/api/ot-requests", "/api/ot-reports")
                        .hasAnyRole("EMPLOYEE", "MANAGER_DEPARTMENT", "MANAGER_OFFICE")

                        .requestMatchers(HttpMethod.GET,
                                "/api/ot-requests/my",
                                "/api/ot-reports/my")
                        .hasAnyRole("MANAGER_OFFICE", "MANAGER_DEPARTMENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/ot-requests/pending")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.GET,
                                "/api/ot-requests",
                                "/api/ot-reports")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/ot-requests/*/decision",
                                "/api/ot-reports/*/decision")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.POST,
                                "/api/ot-sessions/check-in",
                                "/api/ot-sessions/check-out")
                        .hasAnyRole("EMPLOYEE", "MANAGER_DEPARTMENT", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.GET, "/api/ot-sessions/today")
                        .hasAnyRole("EMPLOYEE", "MANAGER_DEPARTMENT", "MANAGER_OFFICE")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}