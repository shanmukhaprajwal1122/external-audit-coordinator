package com.internship.tool.config;

import com.internship.tool.entity.Role;
import com.internship.tool.entity.User;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users into database...");

            // Seed Admin — password: Admin@123
            User admin = User.builder()
                    .fullName("Admin User")
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .department("IT")
                    .isActive(true)
                    .build();
            userRepository.save(admin);

            // Seed Manager — password: Manager@123
            User manager = User.builder()
                    .fullName("Audit Manager")
                    .email("manager@example.com")
                    .passwordHash(passwordEncoder.encode("Manager@123"))
                    .role(Role.MANAGER)
                    .department("Compliance")
                    .isActive(true)
                    .build();
            userRepository.save(manager);

            // Seed Auditor — password: Auditor@123
            User auditor = User.builder()
                    .fullName("Senior Auditor")
                    .email("auditor@example.com")
                    .passwordHash(passwordEncoder.encode("Auditor@123"))
                    .role(Role.AUDITOR)
                    .department("Internal Audit")
                    .isActive(true)
                    .build();
            userRepository.save(auditor);

            log.info("Database seeded successfully with 3 initial users.");
            log.info("  admin@example.com    / Admin@123");
            log.info("  manager@example.com  / Manager@123");
            log.info("  auditor@example.com  / Auditor@123");
        } else {
            log.info("Database already has users — skipping seed.");
        }
    }
}
