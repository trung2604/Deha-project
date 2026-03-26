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
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/offices/**")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.POST, "/api/offices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/offices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/offices/**").hasRole("ADMIN")
                        // Users: department manager can READ only; office manager/admin can CRUD.
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.GET, "/api/users/search").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")

                        .requestMatchers(HttpMethod.POST, "/api/users").hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")

                        // Departments: department managers can READ only their own department.
                        .requestMatchers(HttpMethod.GET, "/api/departments").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.GET, "/api/departments/**").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")

                        // Write operations on departments (and nested positions): admin + office manager only.
                        .requestMatchers(HttpMethod.POST, "/api/departments/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")

                        // Positions (global): office manager/admin only.
                        .requestMatchers("/api/positions").hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers("/api/positions/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")

                        // Payroll & salary contracts: office manager/admin only.
                        .requestMatchers("/api/salary-contracts/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")
                        .requestMatchers("/api/payrolls/**").hasAnyRole("ADMIN", "MANAGER_OFFICE")

                        // Attendance: employees + both manager types + admin.
                        .requestMatchers("/api/attendance/**").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT", "EMPLOYEE")

                        // OT workflow:
                        // - Employees create OT requests/reports
                        // - Managers (office + department) decide OT requests/reports
                        // - Managers can also view pending queues; employees only view "my"
                        .requestMatchers(HttpMethod.POST, "/api/ot-requests").hasAnyRole("ADMIN", "EMPLOYEE", "MANAGER_DEPARTMENT", "MANAGER_OFFICE")
                        .requestMatchers(HttpMethod.POST, "/api/ot-reports").hasAnyRole("ADMIN", "EMPLOYEE", "MANAGER_DEPARTMENT", "MANAGER_OFFICE")

                        .requestMatchers(HttpMethod.GET, "/api/ot-requests/my").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/ot-reports/my").hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/ot-requests/pending")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.GET, "/api/ot-reports/pending")
                        .hasAnyRole("ADMIN", "MANAGER_OFFICE", "MANAGER_DEPARTMENT")

                        .requestMatchers(HttpMethod.PATCH, "/api/ot-requests/*/decision")
                        .hasAnyRole( "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/ot-reports/*/decision")
                        .hasAnyRole( "MANAGER_OFFICE", "MANAGER_DEPARTMENT")
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