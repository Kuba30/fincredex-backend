-- Add company_id expected by Application.java
ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS company_id BIGINT;

-- Rename old report_id to financial_id
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'applications'
              AND column_name = 'report_id'
        )
            AND NOT EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_name = 'applications'
                  AND column_name = 'financial_id'
            )
        THEN
            ALTER TABLE applications
                RENAME COLUMN report_id TO financial_id;
        END IF;
    END $$;

-- Add company foreign key
ALTER TABLE applications
    DROP CONSTRAINT IF EXISTS fk_application_company;

ALTER TABLE applications
    ADD CONSTRAINT fk_application_company
        FOREIGN KEY (company_id)
            REFERENCES companies(id)
            ON DELETE CASCADE;

-- Ensure report FK matches financial_id
ALTER TABLE applications
    DROP CONSTRAINT IF EXISTS fk_app_report;

ALTER TABLE applications
    DROP CONSTRAINT IF EXISTS fk_application_report;

ALTER TABLE applications
    ADD CONSTRAINT fk_application_report
        FOREIGN KEY (financial_id)
            REFERENCES reports(id)
            ON DELETE CASCADE;