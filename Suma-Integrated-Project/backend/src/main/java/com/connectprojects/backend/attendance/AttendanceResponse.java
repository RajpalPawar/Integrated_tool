package com.connectprojects.backend.attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AttendanceResponse(Long id, Long userId, String employeeName, String username,
                                  LocalDate workDate, LocalTime checkIn, LocalTime checkOut, String status,
                                  List<AttendanceBreakResponse> breaks) {
    static AttendanceResponse from(AttendanceRecord record) {
        return new AttendanceResponse(record.getId(), record.getUser().getId(), record.getUser().getFullName(),
                record.getUser().getUsername(), record.getWorkDate(), record.getCheckIn(),
                record.getCheckOut(), record.getStatus(),
                record.getBreaks().stream().map(AttendanceBreakResponse::from).toList());
    }
}
