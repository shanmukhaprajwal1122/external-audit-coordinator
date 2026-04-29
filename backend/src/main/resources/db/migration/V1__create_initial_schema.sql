-- ============================================================
--  V1__create_initial_schema.sql
--  External Audit Coordinator – full initial schema
--  Database: PostgreSQL 15
--  Created: 2026-04-24
-- ============================================================

-- ─── 1. USERS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       NOT NULL,
    full_name       VARCHAR(120)    NOT NULL,
    email           VARCHAR(180)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    department      VARCHAR(100),
    phone_number    VARCHAR(20),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL,

    CONSTRAINT pk_users         PRIMARY KEY (id),
    CONSTRAINT uq_users_email   UNIQUE (email),
    CONSTRAINT chk_users_role   CHECK (role IN ('ADMIN','MANAGER','AUDITOR','VIEWER'))
);

-- ─── 2. AUDIT_PROGRAMS ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_programs (
    id                      BIGSERIAL       NOT NULL,
    title                   VARCHAR(200)    NOT NULL,
    description             TEXT,
    scope                   TEXT,
    objectives              TEXT,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PLANNED',
    planned_start_date      DATE            NOT NULL,
    planned_end_date        DATE            NOT NULL,
    actual_start_date       DATE,
    actual_end_date         DATE,
    department_under_audit  VARCHAR(100),
    is_external             BOOLEAN         NOT NULL DEFAULT TRUE,
    lead_auditor_id         BIGINT          NOT NULL,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL,

    CONSTRAINT pk_audit_programs    PRIMARY KEY (id),
    CONSTRAINT chk_ap_status        CHECK (status IN ('PLANNED','IN_PROGRESS','UNDER_REVIEW','COMPLETED','CANCELLED')),
    CONSTRAINT fk_audit_program_lead_auditor
        FOREIGN KEY (lead_auditor_id) REFERENCES users (id)
);

-- ─── 3. AUDIT_TASKS ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_tasks (
    id               BIGSERIAL    NOT NULL,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PLANNED',
    due_date         DATE,
    completed_date   DATE,
    priority         VARCHAR(10),
    notes            TEXT,
    audit_program_id BIGINT       NOT NULL,
    assigned_to_id   BIGINT,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,

    CONSTRAINT pk_audit_tasks   PRIMARY KEY (id),
    CONSTRAINT chk_at_status    CHECK (status IN ('PLANNED','IN_PROGRESS','UNDER_REVIEW','COMPLETED','CANCELLED')),
    CONSTRAINT fk_audit_task_program
        FOREIGN KEY (audit_program_id) REFERENCES audit_programs (id),
    CONSTRAINT fk_audit_task_assignee
        FOREIGN KEY (assigned_to_id)   REFERENCES users (id)
);

-- ─── 4. DOCUMENTS ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS documents (
    id                  BIGSERIAL       NOT NULL,
    file_name           VARCHAR(255)    NOT NULL,
    original_file_name  VARCHAR(255)    NOT NULL,
    content_type        VARCHAR(100)    NOT NULL,
    file_size_bytes     BIGINT          NOT NULL,
    storage_path        VARCHAR(512)    NOT NULL,
    description         TEXT,
    document_type       VARCHAR(50),
    is_confidential     BOOLEAN         NOT NULL DEFAULT FALSE,
    audit_program_id    BIGINT,
    audit_task_id       BIGINT,
    uploaded_by_id      BIGINT          NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,

    CONSTRAINT pk_documents PRIMARY KEY (id),
    CONSTRAINT fk_document_program
        FOREIGN KEY (audit_program_id) REFERENCES audit_programs (id),
    CONSTRAINT fk_document_task
        FOREIGN KEY (audit_task_id)    REFERENCES audit_tasks (id),
    CONSTRAINT fk_document_uploader
        FOREIGN KEY (uploaded_by_id)   REFERENCES users (id)
);

-- ─── 5. FINDINGS ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS findings (
    id                      BIGSERIAL       NOT NULL,
    title                   VARCHAR(200)    NOT NULL,
    description             TEXT            NOT NULL,
    severity                VARCHAR(20)     NOT NULL,
    recommendation          TEXT,
    management_response     TEXT,
    action_plan             TEXT,
    target_remediation_date DATE,
    actual_remediation_date DATE,
    is_resolved             BOOLEAN         NOT NULL DEFAULT FALSE,
    reference_number        VARCHAR(50),
    audit_program_id        BIGINT          NOT NULL,
    raised_by_id            BIGINT          NOT NULL,
    owner_id                BIGINT,
    created_at              TIMESTAMP       NOT NULL,
    updated_at              TIMESTAMP       NOT NULL,

    CONSTRAINT pk_findings          PRIMARY KEY (id),
    CONSTRAINT uq_findings_ref_num  UNIQUE (reference_number),
    CONSTRAINT chk_findings_severity
        CHECK (severity IN ('CRITICAL','HIGH','MEDIUM','LOW','INFORMATIONAL')),
    CONSTRAINT fk_finding_program
        FOREIGN KEY (audit_program_id) REFERENCES audit_programs (id),
    CONSTRAINT fk_finding_raised_by
        FOREIGN KEY (raised_by_id)     REFERENCES users (id),
    CONSTRAINT fk_finding_owner
        FOREIGN KEY (owner_id)         REFERENCES users (id)
);

-- ─── 6. NOTIFICATIONS ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id                  BIGSERIAL       NOT NULL,
    subject             VARCHAR(200)    NOT NULL,
    message             TEXT            NOT NULL,
    notification_type   VARCHAR(50)     NOT NULL,
    is_read             BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMP,
    resource_type       VARCHAR(50),
    resource_id         BIGINT,
    recipient_id        BIGINT          NOT NULL,
    sender_id           BIGINT,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notification_recipient
        FOREIGN KEY (recipient_id) REFERENCES users (id),
    CONSTRAINT fk_notification_sender
        FOREIGN KEY (sender_id)    REFERENCES users (id)
);

-- ─── INDEXES (performance) ───────────────────────────────────
CREATE INDEX idx_users_email      ON users (email);
CREATE INDEX idx_users_role       ON users (role);

CREATE INDEX idx_ap_status        ON audit_programs (status);
CREATE INDEX idx_ap_lead_auditor  ON audit_programs (lead_auditor_id);

CREATE INDEX idx_at_program       ON audit_tasks (audit_program_id);
CREATE INDEX idx_at_assignee      ON audit_tasks (assigned_to_id);
CREATE INDEX idx_at_status        ON audit_tasks (status);

CREATE INDEX idx_doc_program      ON documents (audit_program_id);
CREATE INDEX idx_doc_task         ON documents (audit_task_id);
CREATE INDEX idx_doc_uploader     ON documents (uploaded_by_id);

CREATE INDEX idx_find_program     ON findings (audit_program_id);
CREATE INDEX idx_find_severity    ON findings (severity);
CREATE INDEX idx_find_resolved    ON findings (is_resolved);

CREATE INDEX idx_notif_recipient  ON notifications (recipient_id);
CREATE INDEX idx_notif_is_read    ON notifications (is_read);
