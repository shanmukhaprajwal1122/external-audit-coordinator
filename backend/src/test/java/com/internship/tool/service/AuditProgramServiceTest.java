package com.internship.tool.service;

import com.internship.tool.entity.AuditProgram;
import com.internship.tool.entity.AuditStatus;
import com.internship.tool.entity.Role;
import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.UnauthorizedAccessException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.AuditProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditProgramServiceTest {

    @Mock
    private AuditProgramRepository auditProgramRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuditProgramService auditProgramService;

    private AuditProgram program;
    private User leadAuditor;

    @BeforeEach
    void setUp() {
        leadAuditor = new User();
        leadAuditor.setId(1L);
        leadAuditor.setRole(Role.MANAGER);
        leadAuditor.setFullName("Lead Auditor");
        leadAuditor.setEmail("lead@test.com");

        program = new AuditProgram();
        program.setId(10L);
        program.setTitle("Test Program");
        program.setStatus(AuditStatus.PLANNED);
        program.setPlannedStartDate(LocalDate.now().plusDays(1));
        program.setPlannedEndDate(LocalDate.now().plusDays(10));
        program.setLeadAuditor(leadAuditor);
    }

    @Test
    void getProgramById_Success() {
        when(auditProgramRepository.findById(10L)).thenReturn(Optional.of(program));
        AuditProgram result = auditProgramService.getProgramById(10L);
        assertNotNull(result);
        assertEquals("Test Program", result.getTitle());
    }

    @Test
    void getProgramById_NotFound() {
        when(auditProgramRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> auditProgramService.getProgramById(99L));
    }

    @Test
    void getAllPrograms_Success() {
        when(auditProgramRepository.findAll()).thenReturn(Arrays.asList(program));
        List<AuditProgram> result = auditProgramService.getAllPrograms();
        assertEquals(1, result.size());
    }

    @Test
    void getAllPrograms_Paginated_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(auditProgramRepository.findAll(pageable)).thenReturn(new PageImpl<>(Arrays.asList(program)));
        Page<AuditProgram> result = auditProgramService.getAllPrograms(pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProgramsByLeadAuditor_Success() {
        when(auditProgramRepository.findByLeadAuditorId(1L)).thenReturn(Arrays.asList(program));
        List<AuditProgram> result = auditProgramService.getProgramsByLeadAuditor(1L);
        assertEquals(1, result.size());
    }

    @Test
    void createProgram_Success() {
        when(userService.getUserById(1L)).thenReturn(leadAuditor);
        when(auditProgramRepository.save(any(AuditProgram.class))).thenReturn(program);

        AuditProgram newProgram = new AuditProgram();
        newProgram.setTitle("New Program");
        newProgram.setPlannedStartDate(LocalDate.now());
        newProgram.setPlannedEndDate(LocalDate.now().plusDays(5));

        AuditProgram result = auditProgramService.createProgram(newProgram, 1L);

        assertNotNull(result);
        assertEquals(AuditStatus.PLANNED, result.getStatus());
        verify(emailService, times(1)).sendTemplateEmail(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void createProgram_InvalidDates_ThrowsValidationException() {
        AuditProgram newProgram = new AuditProgram();
        newProgram.setPlannedStartDate(LocalDate.now().plusDays(5));
        newProgram.setPlannedEndDate(LocalDate.now()); // End before start

        assertThrows(ValidationException.class, () -> auditProgramService.createProgram(newProgram, 1L));
    }

    @Test
    void createProgram_InvalidRole_ThrowsUnauthorizedAccessException() {
        User viewer = new User();
        viewer.setRole(Role.VIEWER);
        when(userService.getUserById(2L)).thenReturn(viewer);

        AuditProgram newProgram = new AuditProgram();
        newProgram.setPlannedStartDate(LocalDate.now());
        newProgram.setPlannedEndDate(LocalDate.now().plusDays(5));

        assertThrows(UnauthorizedAccessException.class, () -> auditProgramService.createProgram(newProgram, 2L));
    }

    @Test
    void updateProgram_Success() {
        when(auditProgramRepository.findById(10L)).thenReturn(Optional.of(program));
        when(auditProgramRepository.save(any(AuditProgram.class))).thenReturn(program);

        AuditProgram details = new AuditProgram();
        details.setTitle("Updated Title");
        details.setPlannedStartDate(LocalDate.now());
        details.setPlannedEndDate(LocalDate.now().plusDays(5));

        AuditProgram result = auditProgramService.updateProgram(10L, details);

        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void updateProgramStatus_ToInProgress_SetsActualStartDate() {
        when(auditProgramRepository.findById(10L)).thenReturn(Optional.of(program));
        when(auditProgramRepository.save(any(AuditProgram.class))).thenReturn(program);

        AuditProgram result = auditProgramService.updateProgramStatus(10L, AuditStatus.IN_PROGRESS);

        assertEquals(AuditStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getActualStartDate());
    }

    @Test
    void deleteProgram_Success() {
        when(auditProgramRepository.findById(10L)).thenReturn(Optional.of(program));
        doNothing().when(auditProgramRepository).delete(program);

        assertDoesNotThrow(() -> auditProgramService.deleteProgram(10L));
        verify(auditProgramRepository, times(1)).delete(program);
    }
}
