package com.internship.tool.service;

import com.internship.tool.entity.AuditProgram;
import com.internship.tool.entity.AuditStatus;
import com.internship.tool.entity.Role;
import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.UnauthorizedAccessException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.AuditProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditProgramService {

    private final AuditProgramRepository auditProgramRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "programs", key = "#id")
    public AuditProgram getProgramById(Long id) {
        return auditProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditProgram", "id", id));
    }

    @Transactional(readOnly = true)
    public List<AuditProgram> getAllPrograms() {
        return auditProgramRepository.findAll();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditProgram> getAllPrograms(org.springframework.data.domain.Pageable pageable) {
        return auditProgramRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditProgram> getProgramsByLeadAuditor(Long leadAuditorId) {
        return auditProgramRepository.findByLeadAuditorId(leadAuditorId);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"programs", "programsList", "programsPaginated"}, allEntries = true)
    public AuditProgram createProgram(AuditProgram program, Long leadAuditorId) {
        validateDates(program.getPlannedStartDate(), program.getPlannedEndDate());
        
        User leadAuditor = userService.getUserById(leadAuditorId);
        if (leadAuditor.getRole() != Role.MANAGER && leadAuditor.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Lead auditor must be a MANAGER or ADMIN.");
        }

        program.setLeadAuditor(leadAuditor);
        if (program.getStatus() == null) {
            program.setStatus(AuditStatus.PLANNED);
        }
        
        AuditProgram saved = auditProgramRepository.save(program);

        // Send creation email notification to lead auditor
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("recipientName", leadAuditor.getFullName());
            vars.put("programTitle", saved.getTitle());
            vars.put("department", saved.getDepartmentUnderAudit() != null ? saved.getDepartmentUnderAudit() : "N/A");
            vars.put("leadAuditor", leadAuditor.getFullName());
            vars.put("startDate", saved.getPlannedStartDate().toString());
            vars.put("endDate", saved.getPlannedEndDate().toString());
            vars.put("status", saved.getStatus().name());
            vars.put("description", saved.getDescription());

            emailService.sendTemplateEmail(
                    leadAuditor.getEmail(),
                    "📋 New Audit Program Assigned: " + saved.getTitle(),
                    "audit-program-created",
                    vars
            );
        } catch (Exception e) {
            log.warn("Email notification failed for program creation id={}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"programs", "programsList", "programsPaginated"}, allEntries = true)
    public AuditProgram updateProgram(Long id, AuditProgram programDetails) {
        AuditProgram program = getProgramById(id);

        validateDates(programDetails.getPlannedStartDate(), programDetails.getPlannedEndDate());

        program.setTitle(programDetails.getTitle());
        program.setDescription(programDetails.getDescription());
        program.setScope(programDetails.getScope());
        program.setObjectives(programDetails.getObjectives());
        program.setPlannedStartDate(programDetails.getPlannedStartDate());
        program.setPlannedEndDate(programDetails.getPlannedEndDate());
        program.setActualStartDate(programDetails.getActualStartDate());
        program.setActualEndDate(programDetails.getActualEndDate());
        program.setDepartmentUnderAudit(programDetails.getDepartmentUnderAudit());
        program.setIsExternal(programDetails.getIsExternal());
        
        return auditProgramRepository.save(program);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"programs", "programsList", "programsPaginated"}, allEntries = true)
    public AuditProgram updateProgramStatus(Long id, AuditStatus newStatus) {
        AuditProgram program = getProgramById(id);
        
        if (newStatus == AuditStatus.IN_PROGRESS && program.getActualStartDate() == null) {
            program.setActualStartDate(LocalDate.now());
        } else if (newStatus == AuditStatus.COMPLETED && program.getActualEndDate() == null) {
             program.setActualEndDate(LocalDate.now());
        }
        
        program.setStatus(newStatus);
        return auditProgramRepository.save(program);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"programs", "programsList", "programsPaginated"}, allEntries = true)
    public void deleteProgram(Long id) {
        AuditProgram program = getProgramById(id);
        auditProgramRepository.delete(program);
    }
    
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ValidationException("End date cannot be before start date.");
        }
    }
}
