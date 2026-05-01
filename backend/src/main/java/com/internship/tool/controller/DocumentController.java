package com.internship.tool.controller;

import com.internship.tool.entity.Document;
import com.internship.tool.service.DocumentService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/all")
    public ResponseEntity<Page<Document>> getAllDocuments(Pageable pageable) {
        Page<Document> documents = documentService.getAllDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocumentMetadata(@PathVariable Long id, @Valid @RequestBody Document documentDetails) {
        Document updatedDocument = documentService.updateDocumentMetadata(id, documentDetails);
        return ResponseEntity.ok(updatedDocument);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
