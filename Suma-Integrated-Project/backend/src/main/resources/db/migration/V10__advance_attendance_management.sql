CREATE TABLE attendance_schedules (
    user_id BIGINT PRIMARY KEY REFERENCES app_users(id) ON DELETE CASCADE,
    shift_minutes INTEGER NOT NULL DEFAULT 540 CHECK (shift_minutes BETWEEN 1 AND 1440),
    break_minutes INTEGER NOT NULL DEFAULT 60 CHECK (break_minutes BETWEEN 0 AND 480),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_breaks (
    id BIGSERIAL PRIMARY KEY,
    attendance_record_id BIGINT NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
    break_type VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    started_at TIME NOT NULL,
    ended_at TIME,
    CONSTRAINT chk_attendance_break_end CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE INDEX idx_attendance_breaks_record ON attendance_breaks(attendance_record_id, started_at);
CREATE UNIQUE INDEX uq_attendance_one_active_break
    ON attendance_breaks(attendance_record_id) WHERE ended_at IS NULL;
