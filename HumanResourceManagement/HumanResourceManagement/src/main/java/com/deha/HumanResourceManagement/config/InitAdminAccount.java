package com.deha.HumanResourceManagement.config;

import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class InitAdminAccount implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("12345678"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(LocalDateTime.now());

            userRepository.save(admin);

            System.out.println(">>> Admin account created!");
        }
    }
}