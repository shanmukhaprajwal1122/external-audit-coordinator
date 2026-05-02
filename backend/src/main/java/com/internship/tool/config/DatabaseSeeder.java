package com.internship.tool.config;

import com.internship.tool.entity.*;
import com.internship.tool.repository.AuditProgramRepository;
import com.internship.tool.repository.AuditTaskRepository;
import com.internship.tool.repository.FindingRepository;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuditProgramRepository auditProgramRepository;
    private final AuditTaskRepository auditTaskRepository;
    private final FindingRepository findingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users into database...");

            User admin = User.builder().fullName("Admin User").email("admin@example.com").passwordHash(passwordEncoder.encode("Admin@123")).role(Role.ADMIN).department("IT").isActive(true).build();
            User manager = User.builder().fullName("Audit Manager").email("manager@example.com").passwordHash(passwordEncoder.encode("Manager@123")).role(Role.MANAGER).department("Compliance").isActive(true).build();
            User auditor = User.builder().fullName("Senior Auditor").email("auditor@example.com").passwordHash(passwordEncoder.encode("Auditor@123")).role(Role.AUDITOR).department("Internal Audit").isActive(true).build();
            
            userRepository.saveAll(List.of(admin, manager, auditor));

            log.info("Seeding 30 demo records (10 Programs, 10 Tasks, 10 Findings)...");
            
            // 10 Programs
            List<AuditProgram> programs = new ArrayList<>();
            AuditStatus[] statuses = AuditStatus.values();
            for (int i = 1; i <= 10; i++) {
                AuditProgram p = AuditProgram.builder()
                        .title("Demo Audit Program " + i)
                        .description("Detailed description and scope for audit program " + i)
                        .status(statuses[i % statuses.length])
                        .plannedStartDate(LocalDate.now().minusDays(30).plusDays(i * 5))
                        .plannedEndDate(LocalDate.now().plusDays(30).plusDays(i * 5))
                        .departmentUnderAudit("Department " + (i % 3))
                        .isExternal(i % 2 == 0)
                        .leadAuditor(manager)
                        .build();
                programs.add(p);
            }
            auditProgramRepository.saveAll(programs);

            // 10 Tasks
            List<AuditTask> tasks = new ArrayList<>();
            String[] priorities = {"HIGH", "MEDIUM", "LOW"};
            for (int i = 1; i <= 10; i++) {
                AuditTask t = AuditTask.builder()
                        .title("Fieldwork Task " + i)
                        .description("Review financial and compliance documents for area " + i)
                        .status(statuses[i % statuses.length])
                        .dueDate(LocalDate.now().plusDays(i * 2))
                        .priority(priorities[i % priorities.length])
                        .auditProgram(programs.get(i % programs.size()))
                        .assignedTo(auditor)
                        .build();
                tasks.add(t);
            }
            auditTaskRepository.saveAll(tasks);

            // 10 Findings
            List<Finding> findings = new ArrayList<>();
            FindingSeverity[] severities = FindingSeverity.values();
            for (int i = 1; i <= 10; i++) {
                Finding f = Finding.builder()
                        .title("Finding Observation " + i)
                        .description("Identified a control gap in process " + i)
                        .severity(severities[i % severities.length])
                        .isResolved(i % 3 == 0)
                        .referenceNumber("FIND-2026-" + String.format("%03d", i))
                        .auditProgram(programs.get(i % programs.size()))
                        .raisedBy(auditor)
                        .owner(manager)
                        .build();
                findings.add(f);
            }
            findingRepository.saveAll(findings);

            log.info("Database seeded successfully with users and 30 demo records.");
        } else {
            log.info("Database already has users — skipping seed.");
        }
    }
}
