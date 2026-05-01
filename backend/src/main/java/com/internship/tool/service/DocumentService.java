package com.internship.tool.service;

import com.internship.tool.entity.AuditProgram;
import com.internship.tool.entity.AuditTask;
import com.internship.tool.entity.Document;
import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.exception.ValidationException;
import com.internship.tool.repository.DocumentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AuditProgramService auditProgramService;
    private final AuditTaskService auditTaskService;
    private final UserService userService;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!");
        }
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "documents", key = "#id")
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByProgramId(Long programId) {
        return documentRepository.findByAuditProgramId(programId);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Document> getAllDocuments(org.springframework.data.domain.Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByTaskId(Long taskId) {
        return documentRepository.findByAuditTaskId(taskId);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"documents", "documentsList", "documentsPaginated"}, allEntries = true)
    public Document uploadDocument(MultipartFile file, Long uploaderId, Long programId, Long taskId, String description, String documentType, Boolean isConfidential) {
        if (programId == null && taskId == null) {
            throw new ValidationException("Document must be associated with either an AuditProgram or an AuditTask.");
        }

        if (file.isEmpty()) {
            throw new ValidationException("Failed to store empty file.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ValidationException("Invalid file type: " + file.getContentType() + ". Allowed types are PDF, Word, Excel, and Images.");
        }

        User uploader = userService.getUserById(uploaderId);
        Document document = new Document();
        document.setUploadedBy(uploader);
        document.setDescription(description);
        document.setDocumentType(documentType);
        document.setIsConfidential(isConfidential != null ? isConfidential : false);

        if (programId != null) {
            AuditProgram program = auditProgramService.getProgramById(programId);
            document.setAuditProgram(program);
        }

        if (taskId != null) {
            AuditTask task = auditTaskService.getTaskById(taskId);
            document.setAuditTask(task);
            if (document.getAuditProgram() == null && task.getAuditProgram() != null) {
                document.setAuditProgram(task.getAuditProgram());
            }
        }

        // Save physical file
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String storedFileName = UUID.randomUUID().toString() + extension;
            Path destinationFile = Paths.get(uploadDir).resolve(Paths.get(storedFileName)).normalize().toAbsolutePath();
            
            if (!destinationFile.getParent().equals(Paths.get(uploadDir).toAbsolutePath())) {
                throw new ValidationException("Cannot store file outside current directory.");
            }
            
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            document.setFileName(storedFileName);
            document.setOriginalFileName(originalFilename);
            document.setContentType(file.getContentType());
            document.setFileSizeBytes(file.getSize());
            document.setStoragePath(destinationFile.toString());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }

        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long id) {
        Document document = getDocumentById(id);
        try {
            Path filePath = Paths.get(uploadDir).resolve(document.getFileName()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found", "id", id);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found", "id", id);
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"documents", "documentsList", "documentsPaginated"}, allEntries = true)
    public Document updateDocumentMetadata(Long id, Document documentDetails) {
        Document document = getDocumentById(id);
        
        if (documentDetails.getDescription() != null) document.setDescription(documentDetails.getDescription());
        if (documentDetails.getDocumentType() != null) document.setDocumentType(documentDetails.getDocumentType());
        if (documentDetails.getIsConfidential() != null) document.setIsConfidential(documentDetails.getIsConfidential());

        return documentRepository.save(document);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"documents", "documentsList", "documentsPaginated"}, allEntries = true)
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);
        
        try {
            Path filePath = Paths.get(document.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Could not delete physical file: {}", document.getStoragePath(), e);
        }
        
        documentRepository.delete(document);
    }
}
