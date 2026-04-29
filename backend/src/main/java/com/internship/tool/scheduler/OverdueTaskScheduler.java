package com.internship.tool.scheduler;

import com.internship.tool.entity.AuditStatus;
import com.internship.tool.entity.AuditTask;
import com.internship.tool.repository.AuditTaskRepository;
import com.internship.tool.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueTaskScheduler {

    private final AuditTaskRepository auditTaskRepository;
    private final EmailService emailService;

    /**
     * Runs every day at 8:00 AM server time.
     * Finds all tasks that are past their due date and not yet COMPLETED,
     * then fires an email alert to the assigned auditor and the lead auditor.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverdueTasks() {
        LocalDate today = LocalDate.now();
        log.info("Running overdue task check for date: {}", today);

        List<AuditTask> overdueTasks = auditTaskRepository.findByDueDateBeforeAndStatusNot(today, AuditStatus.COMPLETED);

        if (overdueTasks.isEmpty()) {
            log.info("No overdue tasks found.");
            return;
        }

        log.info("Found {} overdue task(s). Sending alerts...", overdueTasks.size());

        for (AuditTask task : overdueTasks) {
            long daysOverdue = ChronoUnit.DAYS.between(task.getDueDate(), today);
            String programTitle = task.getAuditProgram() != null ? task.getAuditProgram().getTitle() : "N/A";

            // Notify the assigned auditor
            if (task.getAssignedTo() != null && task.getAssignedTo().getEmail() != null) {
                Map<String, Object> vars = new HashMap<>();
                vars.put("recipientName", task.getAssignedTo().getFullName());
                vars.put("taskTitle", task.getTitle());
                vars.put("programTitle", programTitle);
                vars.put("assigneeName", task.getAssignedTo().getFullName());
                vars.put("dueDate", task.getDueDate().toString());
                vars.put("daysOverdue", daysOverdue);
                vars.put("priority", task.getPriority() != null ? task.getPriority() : "N/A");
                vars.put("status", task.getStatus().name());

                emailService.sendTemplateEmail(
                        task.getAssignedTo().getEmail(),
                        "⚠️ Overdue Task Alert: " + task.getTitle(),
                        "overdue-task-alert",
                        vars
                );
            }

            // Also notify the lead auditor of the program (if different)
            if (task.getAuditProgram() != null
                    && task.getAuditProgram().getLeadAuditor() != null
                    && !task.getAuditProgram().getLeadAuditor().equals(task.getAssignedTo())) {

                Map<String, Object> managerVars = new HashMap<>();
                managerVars.put("recipientName", task.getAuditProgram().getLeadAuditor().getFullName());
                managerVars.put("taskTitle", task.getTitle());
                managerVars.put("programTitle", programTitle);
                managerVars.put("assigneeName", task.getAssignedTo() != null ? task.getAssignedTo().getFullName() : "Unassigned");
                managerVars.put("dueDate", task.getDueDate().toString());
                managerVars.put("daysOverdue", daysOverdue);
                managerVars.put("priority", task.getPriority() != null ? task.getPriority() : "N/A");
                managerVars.put("status", task.getStatus().name());

                emailService.sendTemplateEmail(
                        task.getAuditProgram().getLeadAuditor().getEmail(),
                        "⚠️ [Manager Alert] Overdue Task: " + task.getTitle(),
                        "overdue-task-alert",
                        managerVars
                );
            }
        }
        log.info("Overdue task email alerts dispatched.");
    }
}
