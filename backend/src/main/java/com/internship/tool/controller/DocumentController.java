package com.internship.tool.controller;

import com.internship.tool.entity.Document;
import com.internship.tool.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Documents", description = "Endpoints for managing file uploads and document metadata")
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Get all documents", description = "Retrieves a paginated list of all document metadata.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping("/all")
    public ResponseEntity<Page<Document>> getAllDocuments(Pageable pageable) {
        Page<Document> documents = documentService.getAllDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    @Operation(summary = "Get document metadata by ID", description = "Retrieves metadata for a specific document.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved document")
    @ApiResponse(responseCode = "404", description = "Document not found")
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    @Operation(summary = "Upload a document", description = "Uploads a physical file and associates it with a program or task.")
    @ApiResponse(responseCode = "201", description = "Document successfully uploaded")
    @ApiResponse(responseCode = "400", description = "Invalid file or metadata")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long uploaderId,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false, defaultValue = "false") Boolean isConfidential) {

        Document uploadedDocument = documentService.uploadDocument(
                file, uploaderId, programId, taskId, description, documentType, isConfidential);
        return new ResponseEntity<>(uploadedDocument, HttpStatus.CREATED);
    }

    @Operation(summary = "Download file", description = "Downloads the physical file content.")
    @ApiResponse(responseCode = "200", description = "File stream returned")
    @ApiResponse(responseCode = "404", description = "File not found on disk")
    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpServletRequest request) {
        Resource resource = documentService.loadFileAsResource(id);
        Document document = documentService.getDocumentById(id);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }

    @Operation(summary = "Update document metadata", description = "Updates metadata (description, type, etc.) for a document.")
    @ApiResponse(responseCode = "200", description = "Metadata successfully updated")
    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocumentMetadata(@PathVariable Long id, @Valid @RequestBody Document documentDetails) {
        Document updatedDocument = documentService.updateDocumentMetadata(id, documentDetails);
        return ResponseEntity.ok(updatedDocument);
    }

    @Operation(summary = "Delete a document", description = "Deletes both the metadata and physical file from the server.")
    @ApiResponse(responseCode = "204", description = "Document successfully deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}

