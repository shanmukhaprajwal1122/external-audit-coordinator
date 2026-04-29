-- ============================================================
--  V2__fix_users_role_constraint.sql
--  Adds AUDITOR to the users.role CHECK constraint.
--  V1 was missing AUDITOR which caused DatabaseSeeder to fail.
-- ============================================================

-- Drop the old constraint and recreate with AUDITOR included
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'MANAGER', 'AUDITOR', 'VIEWER'));
