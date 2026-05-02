package com.internship.tool.controller;

import com.internship.tool.entity.AuditProgram;
import com.internship.tool.entity.AuditStatus;
import com.internship.tool.service.AuditProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Audit Programs", description = "Endpoints for managing external and internal audit programs")
@RestController
@RequestMapping("/api/audit-programs")
@RequiredArgsConstructor
public class AuditProgramController {

    private final AuditProgramService auditProgramService;

    @Operation(summary = "Get all programs", description = "Retrieves a paginated list of all audit programs.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public ResponseEntity<Page<AuditProgram>> getAllPrograms(Pageable pageable) {
        Page<AuditProgram> programs = auditProgramService.getAllPrograms(pageable);
        return ResponseEntity.ok(programs);
    }

    @Operation(summary = "Get a program by ID", description = "Retrieves a specific audit program by its ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved program")
    @ApiResponse(responseCode = "404", description = "Program not found")
    @GetMapping("/{id}")
    public ResponseEntity<AuditProgram> getProgramById(@PathVariable Long id) {
        AuditProgram program = auditProgramService.getProgramById(id);
        return ResponseEntity.ok(program);
    }

    @Operation(summary = "Create an audit program", description = "Creates a new audit program and assigns a lead auditor.")
    @ApiResponse(responseCode = "201", description = "Program successfully created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @PostMapping("/create")
    public ResponseEntity<AuditProgram> createProgram(@Valid @RequestBody AuditProgram program, @RequestParam Long leadAuditorId) {
        AuditProgram createdProgram = auditProgramService.createProgram(program, leadAuditorId);
        return new ResponseEntity<>(createdProgram, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an audit program", description = "Updates the general details of an audit program.")
    @ApiResponse(responseCode = "200", description = "Program successfully updated")
    @ApiResponse(responseCode = "404", description = "Program not found")
    @PutMapping("/{id}")
    public ResponseEntity<AuditProgram> updateProgram(@PathVariable Long id, @Valid @RequestBody AuditProgram programDetails) {
        AuditProgram updatedProgram = auditProgramService.updateProgram(id, programDetails);
        return ResponseEntity.ok(updatedProgram);
    }

    @Operation(summary = "Update program status", description = "Changes the lifecycle status of an audit program (e.g., IN_PROGRESS, COMPLETED).")
    @ApiResponse(responseCode = "200", description = "Status successfully updated")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AuditProgram> updateProgramStatus(@PathVariable Long id, @RequestParam AuditStatus status) {
        AuditProgram updatedProgram = auditProgramService.updateProgramStatus(id, status);
        return ResponseEntity.ok(updatedProgram);
    }

    @Operation(summary = "Delete an audit program", description = "Deletes an audit program from the system by ID.")
    @ApiResponse(responseCode = "204", description = "Program successfully deleted")
    @ApiResponse(responseCode = "404", description = "Program not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        auditProgramService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }
}
