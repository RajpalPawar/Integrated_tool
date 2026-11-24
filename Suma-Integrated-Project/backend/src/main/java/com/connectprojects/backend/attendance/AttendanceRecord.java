package com.connectprojects.backend.attendance;

import com.connectprojects.backend.account.AppUser;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attendance_records", uniqueConstraints =
        @UniqueConstraint(name = "uk_attendance_user_work_date", columnNames = {"user_id", "work_date"}))
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    @Column(nullable = false, length = 20)
    private String status = "PRESENT";

    @OneToMany(mappedBy = "attendanceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startedAt ASC")
    private List<AttendanceBreak> breaks = new ArrayList<>();

    protected AttendanceRecord() { }

    public AttendanceRecord(AppUser user, LocalDate workDate, LocalTime checkIn) {
        this.user = user;
        this.workDate = workDate;
        this.checkIn = checkIn;
    }

    public void checkOut(LocalTime time) {
        if (checkOut != null) throw new IllegalStateException("You have already checked out today.");
        if (checkIn == null || time.isBefore(checkIn)) throw new IllegalStateException("Check-out time cannot be before check-in.");
        if (breaks.stream().anyMatch(AttendanceBreak::isActive)) {
            throw new IllegalStateException("End your break before checking out.");
        }
        this.checkOut = time;
    }

    public void startBreak(String breakType, LocalTime time) {
        if (checkIn == null) throw new IllegalStateException("Check in before starting a break.");
        if (checkOut != null) throw new IllegalStateException("You have already checked out today.");
        if (time.isBefore(checkIn)) throw new IllegalStateException("Break cannot start before check-in.");
        if (breaks.stream().anyMatch(AttendanceBreak::isActive)) {
            throw new IllegalStateException("A break is already in progress.");
        }
        breaks.add(new AttendanceBreak(this, breakType, time));
    }

    public void endBreak(LocalTime time) {
        AttendanceBreak current = breaks.stream()
                .filter(AttendanceBreak::isActive)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("Start a break before ending it."));
        current.end(time);
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalTime getCheckIn() { return checkIn; }
    public LocalTime getCheckOut() { return checkOut; }
    public String getStatus() { return status; }
    public List<AttendanceBreak> getBreaks() { return breaks; }
}
