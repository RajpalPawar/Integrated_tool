package com.connectprojects.backend.attendance;

import java.time.LocalTime;

public record AttendanceBreakResponse(Long id, String breakType, LocalTime startedAt, LocalTime endedAt,
                                      long durationMinutes, boolean active) {
    public static AttendanceBreakResponse from(AttendanceBreak attendanceBreak) {
        return new AttendanceBreakResponse(attendanceBreak.getId(), attendanceBreak.getBreakType(),
                attendanceBreak.getStartedAt(), attendanceBreak.getEndedAt(),
                attendanceBreak.getDurationMinutes(), attendanceBreak.isActive());
    }
}
