package com.connectprojects.backend.attendance;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_breaks")
public class AttendanceBreak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_record_id", nullable = false)
    private AttendanceRecord attendanceRecord;

    @Column(name = "break_type", nullable = false, length = 30)
    private String breakType;

    @Column(name = "started_at", nullable = false)
    private LocalTime startedAt;

    @Column(name = "ended_at")
    private LocalTime endedAt;

    protected AttendanceBreak() { }

    AttendanceBreak(AttendanceRecord attendanceRecord, String breakType, LocalTime startedAt) {
        this.attendanceRecord = attendanceRecord;
        this.breakType = breakType;
        this.startedAt = startedAt;
    }

    void end(LocalTime time) {
        if (endedAt != null) throw new IllegalStateException("This break has already ended.");
        if (time.isBefore(startedAt)) throw new IllegalStateException("Break end cannot be before its start.");
        endedAt = time;
    }

    public Long getId() { return id; }
    public String getBreakType() { return breakType; }
    public LocalTime getStartedAt() { return startedAt; }
    public LocalTime getEndedAt() { return endedAt; }
    public boolean isActive() { return endedAt == null; }
    public long getDurationMinutes() {
        return endedAt == null ? 0 : Duration.between(startedAt, endedAt).toMinutes();
    }
}
