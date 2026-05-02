package com.internship.tool.controller;

import com.internship.tool.entity.Finding;
import com.internship.tool.service.FindingService;
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

@Tag(name = "Findings", description = "Endpoints for managing audit findings and observations")
@RestController
@RequestMapping("/api/findings")
@RequiredArgsConstructor
public class FindingController {

    private final FindingService findingService;

    @Operation(summary = "Get all findings", description = "Retrieves a paginated list of all findings.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public ResponseEntity<Page<Finding>> getAllFindings(Pageable pageable) {
        Page<Finding> findings = findingService.getAllFindings(pageable);
        return ResponseEntity.ok(findings);
    }

    @Operation(summary = "Get a finding by ID", description = "Retrieves a specific finding by its ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved finding")
    @ApiResponse(responseCode = "404", description = "Finding not found")
    @GetMapping("/{id}")
    public ResponseEntity<Finding> getFindingById(@PathVariable Long id) {
        Finding finding = findingService.getFindingById(id);
        return ResponseEntity.ok(finding);
    }

    @Operation(summary = "Create a new finding", description = "Logs a new audit finding under a program.")
    @ApiResponse(responseCode = "201", description = "Finding successfully created")
    @PostMapping("/create")
    public ResponseEntity<Finding> createFinding(
            @Valid @RequestBody Finding finding,
            @RequestParam Long programId,
            @RequestParam Long raisedById) {
        Finding createdFinding = findingService.createFinding(programId, raisedById, finding);
        return new ResponseEntity<>(createdFinding, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing finding", description = "Updates the general details of a finding.")
    @ApiResponse(responseCode = "200", description = "Finding successfully updated")
    @PutMapping("/{id}")
    public ResponseEntity<Finding> updateFinding(@PathVariable Long id, @Valid @RequestBody Finding findingDetails) {
        Finding updatedFinding = findingService.updateFinding(id, findingDetails);
        return ResponseEntity.ok(updatedFinding);
    }

    @Operation(summary = "Resolve finding", description = "Marks a finding as resolved.")
    @ApiResponse(responseCode = "200", description = "Finding successfully resolved")
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Finding> resolveFinding(@PathVariable Long id) {
        Finding resolvedFinding = findingService.resolveFinding(id);
        return ResponseEntity.ok(resolvedFinding);
    }

    @Operation(summary = "Assign a finding owner", description = "Assigns an owner responsible for remediating the finding.")
    @ApiResponse(responseCode = "200", description = "Owner successfully assigned")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Finding> assignOwner(@PathVariable Long id, @RequestParam Long ownerId) {
        Finding updatedFinding = findingService.assignOwner(id, ownerId);
        return ResponseEntity.ok(updatedFinding);
    }

    @Operation(summary = "Delete a finding", description = "Deletes an audit finding from the system.")
    @ApiResponse(responseCode = "204", description = "Finding successfully deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFinding(@PathVariable Long id) {
        findingService.deleteFinding(id);
        return ResponseEntity.noContent().build();
    }
}

