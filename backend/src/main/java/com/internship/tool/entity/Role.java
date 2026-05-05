package com.internship.tool.entity;

/**
 * Roles a user can hold within the External Audit Coordinator system.
 *
 * ADMIN   — full system access; manages users and audit programs
 * MANAGER — creates/manages audit programs and reviews findings
 * VIEWER  — read-only access to assigned programs and reports
 */
public enum Role {
    ADMIN,
    AUDITOR,
    MANAGER,
    VIEWER
}
