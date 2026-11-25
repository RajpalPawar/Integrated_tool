CREATE TABLE employee_directory (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(40) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    department VARCHAR(120) NOT NULL,
    job_title VARCHAR(120) NOT NULL,
    work_email VARCHAR(254),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    monthly_salary_encrypted VARCHAR(512) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_employee_directory_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX ix_employee_directory_status_name
    ON employee_directory(status, full_name);
