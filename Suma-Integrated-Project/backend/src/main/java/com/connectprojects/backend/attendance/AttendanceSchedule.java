package com.connectprojects.backend.attendance;

import com.connectprojects.backend.account.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "attendance_schedules")
public class AttendanceSchedule {
    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "shift_minutes", nullable = false)
    private int shiftMinutes = 540;

    @Column(name = "break_minutes", nullable = false)
    private int breakMinutes = 60;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected AttendanceSchedule() { }

    public AttendanceSchedule(AppUser user, int shiftMinutes, int breakMinutes) {
        this.user = user;
        this.shiftMinutes = shiftMinutes;
        this.breakMinutes = breakMinutes;
    }

    public void update(int shiftMinutes, int breakMinutes) {
        this.shiftMinutes = shiftMinutes;
        this.breakMinutes = breakMinutes;
        this.updatedAt = Instant.now();
    }

    public Long getUserId() { return userId; }
    public AppUser getUser() { return user; }
    public int getShiftMinutes() { return shiftMinutes; }
    public int getBreakMinutes() { return breakMinutes; }
    public Instant getUpdatedAt() { return updatedAt; }
}
