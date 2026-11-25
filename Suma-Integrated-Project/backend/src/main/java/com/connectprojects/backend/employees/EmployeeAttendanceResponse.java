package com.connectprojects.backend.employees;

import com.connectprojects.backend.attendance.AttendanceBreakResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EmployeeAttendanceResponse(
        Long employeeRecordId,
        String employeeId,
        String employeeName,
        String linkedUsername,
        LocalDate workDate,
        LocalTime checkIn,
        LocalTime checkOut,
        List<AttendanceBreakResponse> breaks
) {
}
