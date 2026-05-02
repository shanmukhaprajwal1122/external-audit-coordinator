package com.internship.tool.controller;

import com.internship.tool.entity.AuditStatus;
import com.internship.tool.entity.AuditTask;
import com.internship.tool.service.AuditTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Audit Tasks", description = "Endpoints for managing individual audit tasks within a program")
@RestController
@RequestMapping("/api/audit-tasks")
@RequiredArgsConstructor
public class AuditTaskController {

    private final AuditTaskService auditTaskService;

    @Operation(summary = "Get all tasks", description = "Retrieves a paginated list of all audit tasks.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public ResponseEntity<Page<AuditTask>> getAllTasks(Pageable pageable) {
        Page<AuditTask> tasks = auditTaskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Get a task by ID", description = "Retrieves a specific audit task by its ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved task")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @GetMapping("/{id}")
    public ResponseEntity<AuditTask> getTaskById(@PathVariable Long id) {
        AuditTask task = auditTaskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Create an audit task", description = "Creates a new audit task linked to a program and an optional assignee.")
    @ApiResponse(responseCode = "201", description = "Task successfully created")
    @PostMapping("/create")
    public ResponseEntity<AuditTask> createTask(@Valid @RequestBody AuditTask task, @RequestParam Long programId, @RequestParam(required = false) Long assigneeId) {
        AuditTask createdTask = auditTaskService.createTask(programId, assigneeId, task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an audit task", description = "Updates the details of an existing audit task.")
    @ApiResponse(responseCode = "200", description = "Task successfully updated")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @PutMapping("/{id}")
    public ResponseEntity<AuditTask> updateTask(@PathVariable Long id, @Valid @RequestBody AuditTask taskDetails) {
        AuditTask updatedTask = auditTaskService.updateTask(id, taskDetails);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Update task status", description = "Changes the status of a specific task.")
    @ApiResponse(responseCode = "200", description = "Status successfully updated")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AuditTask> updateTaskStatus(@PathVariable Long id, @RequestParam AuditStatus status) {
        AuditTask updatedTask = auditTaskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Assign a task", description = "Re-assigns a task to a different user.")
    @ApiResponse(responseCode = "200", description = "Task successfully assigned")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<AuditTask> assignTask(@PathVariable Long id, @RequestParam Long assigneeId) {
        AuditTask updatedTask = auditTaskService.assignTask(id, assigneeId);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Delete an audit task", description = "Deletes an audit task from the system.")
    @ApiResponse(responseCode = "204", description = "Task successfully deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        auditTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}

