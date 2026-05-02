package com.internship.tool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An observation or non-conformance raised during an audit program.
 * Findings have a severity level and track remediation progress.
 */
@Entity
@Table(name = "findings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private FindingSeverity severity;

    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "management_response", columnDefinition = "TEXT")
    private String managementResponse;

    @Column(name = "action_plan", columnDefinition = "TEXT")
    private String actionPlan;

    @Column(name = "target_remediation_date")
    private LocalDate targetRemediationDate;

    @Column(name = "actual_remediation_date")
    private LocalDate actualRemediationDate;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @Column(name = "reference_number", unique = true, length = 50)
    private String referenceNumber;   // e.g. "FIND-2024-001"

    // ─── Auditing ────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─── Relationships ────────────────────────────────────────────────────────

    /** The audit program in which this finding was raised. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_program_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_finding_program"))
    private AuditProgram auditProgram;

    /** The auditor who raised this finding. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_finding_raised_by"))
    private User raisedBy;

    /** The auditee responsible for remediation (nullable until assigned). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id",
            foreignKey = @ForeignKey(name = "fk_finding_owner"))
    private User owner;
}
