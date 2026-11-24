package com.connectprojects.backend.attendance;

public record AttendanceScheduleResponse(Long userId, String employeeName, String username,
                                         int shiftMinutes, int breakMinutes) {
    static AttendanceScheduleResponse from(AttendanceSchedule schedule) {
        return new AttendanceScheduleResponse(schedule.getUserId(), schedule.getUser().getFullName(),
                schedule.getUser().getUsername(), schedule.getShiftMinutes(), schedule.getBreakMinutes());
    }
}
