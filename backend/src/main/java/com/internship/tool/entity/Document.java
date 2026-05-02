package com.internship.tool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents a file or report uploaded as evidence or deliverable
 * for an {@link AuditProgram} or a specific {@link AuditTask}.
 */
@Entity
@Table(name = "documents")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "document_type", length = 50)
    private String documentType;   // e.g. EVIDENCE, REPORT, CHECKLIST, REQUEST_LETTER

    @Column(name = "is_confidential", nullable = false)
    @Builder.Default
    private Boolean isConfidential = false;

    // ─── Auditing ────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─── Relationships ────────────────────────────────────────────────────────

    /** The audit program this document is associated with (nullable if attached to a task). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_program_id",
            foreignKey = @ForeignKey(name = "fk_document_program"))
    private AuditProgram auditProgram;

    /** The specific task this document is attached to (nullable if at program level). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_task_id",
            foreignKey = @ForeignKey(name = "fk_document_task"))
    private AuditTask auditTask;

    /** The user who uploaded this document. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_document_uploader"))
    private User uploadedBy;
}
