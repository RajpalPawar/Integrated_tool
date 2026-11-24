CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    work_date DATE NOT NULL,
    check_in TIME,
    check_out TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    CONSTRAINT uk_attendance_user_work_date UNIQUE (user_id, work_date),
    CONSTRAINT chk_attendance_checkout_after_checkin CHECK (check_out IS NULL OR check_in IS NOT NULL AND check_out >= check_in)
);

CREATE INDEX idx_attendance_work_date ON attendance_records(work_date);
